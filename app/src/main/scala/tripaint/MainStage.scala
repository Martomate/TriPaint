package tripaint

import tripaint.ScalaFxExt.{fromFXColor, toFXColor, toScala}
import tripaint.effects.{BlurEffect, MotionBlurEffect, RandomNoiseEffect}
import tripaint.grid.GridCell
import tripaint.image.{ImagePool, ImageStorage}
import tripaint.image.format.{RecursiveStorageFormat, SimpleStorageFormat}
import tripaint.util.{createResource, Resource}
import tripaint.view.*
import tripaint.view.gui.*
import tripaint.view.image.ImageGridPane

import javafx.scene.Scene
import javafx.scene.control.{
  Alert,
  ButtonType,
  ColorPicker,
  Label,
  MenuBar,
  TextInputDialog,
  ToolBar
}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.layout.{AnchorPane, BorderPane, TilePane, VBox}
import javafx.scene.paint.Color as FXColor
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage

import java.io.File
import scala.language.implicitConversions
import scala.util.Try

class MainStage(controls: TriPaintViewListener, model: TriPaintModel)
    extends Stage
    with TriPaintView {
  private val (currentEditMode, setCurrentEditMode) = createResource(EditMode.Draw)

  private val imageDisplay: ImageGridPane = new ImageGridPane(model.imageGrid, currentEditMode)

  private val menuBar: MenuBar = TheMenuBar.create(controls)
  private val toolBar: ToolBar = TheToolBar.create(controls)
  private val toolBox: TilePane = ToolBox.create(EditMode.all, currentEditMode, setCurrentEditMode)
  private val imageTabs: TilePane =
    ImageTabs.fromImagePool(model.imageGrid, model.imagePool, controls.requestImageRemoval)
  private val colorBox: VBox = makeColorBox()

  private var currentFolder: Option[File] = None

  {
    this.setTitle("TriPaint")
    this.setOnCloseRequest(e => {
      if (!controls.requestExit()) e.consume()
    })

    val centerPane = new AnchorPane(imageDisplay, toolBox, colorBox, imageTabs)
    AnchorPane.setTopAnchor(imageDisplay, 0)
    AnchorPane.setRightAnchor(imageDisplay, 0)
    AnchorPane.setBottomAnchor(imageDisplay, 0)
    AnchorPane.setLeftAnchor(imageDisplay, 0)
    imageDisplay.clipProperty.isEqualTo(centerPane.clipProperty)

    AnchorPane.setLeftAnchor(toolBox, 0)
    AnchorPane.setTopAnchor(toolBox, 0)

    AnchorPane.setLeftAnchor(colorBox, 10)
    AnchorPane.setBottomAnchor(colorBox, 10)

    AnchorPane.setRightAnchor(imageTabs, 10)
    AnchorPane.setTopAnchor(imageTabs, 10)
    AnchorPane.setBottomAnchor(imageTabs, 10)

    val topPane = new VBox(menuBar, toolBar)

    val sceneContents = new BorderPane(centerPane, topPane, null, null, null)

    val scene = new Scene(sceneContents, 720, 720)
    scene.getStylesheets.add(getClass.getResource("/styles/application.css").toExternalForm)

    for m <- EditMode.all do {
      if m.shortCut != null then {
        scene.getAccelerators.put(m.shortCut, () => setCurrentEditMode(m))
      }
    }

    this.setScene(scene)
  }

  private def makeColorBox() = {
    // overlay and imageDisplay
    val colorPicker1 = new ColorPicker(imageDisplay.colors.primaryColor.get().toFXColor)
    val colorPicker2 = new ColorPicker(imageDisplay.colors.secondaryColor.get().toFXColor)

    colorPicker1.valueProperty.addListener: (_, from, to) =>
      if from != to then imageDisplay.colors.setPrimaryColor(fromFXColor(to))

    colorPicker2.valueProperty.addListener: (_, from, to) =>
      if from != to then imageDisplay.colors.setSecondaryColor(fromFXColor(to))

    imageDisplay.colors.primaryColor.addListener: (_, from, to) =>
      if from != to then colorPicker1.setValue(to.toFXColor)

    imageDisplay.colors.secondaryColor.addListener: (_, from, to) =>
      if from != to then colorPicker2.setValue(to.toFXColor)

    new VBox(
      new Label("Primary color:"),
      colorPicker1,
      new Label("Secondary color:"),
      colorPicker2
    )
  }

  override def backgroundColor: Color = imageDisplay.colors.secondaryColor.get()

  override def askForSaveFile(image: GridCell): Option[File] = {
    val chooser = new FileChooser
    currentFolder.foreach(chooser.setInitialDirectory)
    chooser.setTitle("Save file")
    chooser.getExtensionFilters.add(new ExtensionFilter("PNG", "*.png"))
    val result = Option(chooser.showSaveDialog(this))
    result.foreach(r => currentFolder = Some(r.getParentFile))
    result
  }

  override def askForFileSaveSettings(file: File, image: GridCell): Option[FileSaveSettings] = {
    AskForFileSaveSettingsDialog.askForFileSaveSettings(
      image.storage,
      file,
      Seq(
        SimpleStorageFormat -> "Simple format",
        RecursiveStorageFormat -> "Recursive format"
      ),
      0
    )
  }

  override def askForFileToOpen(): Option[File] = {
    val chooser = new FileChooser
    chooser.setTitle("Open file")
    val result = Option(chooser.showOpenDialog(this))
    result.foreach(r => currentFolder = Some(r.getParentFile))
    result
  }

  override def askForWhereToPutImage(): Option[(Int, Int)] = {
    AskForXYDialog.askForXY(
      title = "New image",
      headerText = "Please enter where it should be placed."
    )
  }

  override def askForBlurRadius(): Option[Int] = {
    val selectedImagesCoords = model.imageGrid.selectedImages.map(_.coords)
    DialogUtils.getValueFromDialog[Int](
      model.imagePool,
      model.imageGrid.selectedImages,
      "Blur images",
      "How much should the images be blurred?",
      "Radius:",
      TextFieldRestriction.uintRestriction,
      str => Try(str.toInt).getOrElse(0),
      Some((radius, grid) =>
        if radius > 0 then new BlurEffect(radius).action(selectedImagesCoords, grid)
      )
    )
  }

  override def askForMotionBlurRadius(): Option[Int] = {
    val selectedImagesCoords = model.imageGrid.selectedImages.map(_.coords)
    DialogUtils.getValueFromDialog[Int](
      model.imagePool,
      model.imageGrid.selectedImages,
      "Motion blur images",
      "How much should the images be motion blurred?",
      "Radius:",
      TextFieldRestriction.uintRestriction,
      str => Try(str.toInt).getOrElse(0),
      Some((radius, grid) =>
        if radius > 0 then new MotionBlurEffect(radius).action(selectedImagesCoords, grid)
      )
    )
  }

  override def askForRandomNoiseColors(): Option[(Color, Color)] = {
    val images = model.imageGrid.selectedImages
    val selectedImagesCoords = model.imageGrid.selectedImages.map(_.coords)
    val loColorPicker = new ColorPicker(FXColor.BLACK)
    val hiColorPicker = new ColorPicker(FXColor.WHITE)
    import DialogUtils.*

    val (previewPane, updatePreview) = DialogUtils.makeImagePreviewList(images, model.imagePool)

    def updatePreviewFromInputs(): Unit =
      val lo = fromFXColor(loColorPicker.getValue)
      val hi = fromFXColor(hiColorPicker.getValue)
      updatePreview(grid => new RandomNoiseEffect(lo, hi).action(selectedImagesCoords, grid))

    loColorPicker.valueProperty.addListener((_, _, _) => updatePreviewFromInputs())
    hiColorPicker.valueProperty.addListener((_, _, _) => updatePreviewFromInputs())

    updatePreviewFromInputs()

    getValueFromCustomDialog[(Color, Color)](
      title = "Fill images randomly",
      headerText = "Which color-range should be used?",
      graphic = previewPane,
      content = Seq(
        makeGridPane(
          Seq(
            Seq(new Label("Minimum color:"), loColorPicker),
            Seq(new Label("Maximum color:"), hiColorPicker)
          )
        )
      ),
      resultConverter = {
        case ButtonType.OK =>
          Try((loColorPicker.getValue, hiColorPicker.getValue))
            .map((lo, hi) => (fromFXColor(lo), fromFXColor(hi)))
            .getOrElse(null)
        case _ => null
      },
      buttons = Seq(ButtonType.OK, ButtonType.CANCEL)
    )
  }

  override def askSaveBeforeClosing(images: Seq[GridCell]): Option[Boolean] = {
    val res = saveBeforeClosingAlert(images).showAndWait().toScala

    res.flatMap(_.getButtonData match {
      case ButtonData.YES => Some(true)
      case ButtonData.NO  => Some(false)
      case _              => None
    })
  }

  private def saveBeforeClosingAlert(images: Seq[GridCell]): Alert = {
    val (previewPane, _) = DialogUtils.makeImagePreviewList(images, model.imagePool)

    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Save before closing?")
    alert.setHeaderText(
      "Do you want to save " + (if (images.size == 1) "this image"
                                else "these images") + " before closing the tab?"
    )
    alert.setGraphic(previewPane)

    alert.getButtonTypes.setAll(
      new ButtonType("Save", ButtonData.YES),
      new ButtonType("Don't save", ButtonData.NO),
      new ButtonType("Cancel", ButtonData.CANCEL_CLOSE)
    )
    alert
  }

  override def askForFileOpenSettings(
      file: File,
      imageSize: Int,
      xCount: Int,
      yCount: Int
  ): Option[FileOpenSettings] = {
    AskForFileOpenSettingsDialog.askForFileOpenSettings(
      imagePreview = (file, imageSize, xCount, yCount),
      Seq(
        SimpleStorageFormat -> "Simple format",
        RecursiveStorageFormat -> "Recursive format"
      ),
      0,
      model.fileSystem.readImage
    )
  }

  override def shouldReplaceImage(
      currentImage: ImageStorage,
      newImage: ImageStorage,
      location: ImagePool.SaveLocation
  ): Option[Boolean] = {
    val tri1 = model.imageGrid.findByStorage(newImage).orNull
    val tri2 = model.imageGrid.findByStorage(currentImage).orNull
    val (previewPane, _) = DialogUtils.makeImagePreviewList(Seq(tri1, tri2), model.imagePool)

    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Collision")
    alert.setHeaderText("The image already exists on the screen. Which one should be used?")
    alert.setGraphic(previewPane)

    alert.getButtonTypes.setAll(
      new ButtonType("Left", ButtonData.YES),
      new ButtonType("Right", ButtonData.NO),
      new ButtonType("Cancel", ButtonData.CANCEL_CLOSE)
    )
    alert.showAndWait().toScala.map(_.getButtonData == ButtonData.YES)
  }

  override def askForImageSize(): Option[Int] = {
    val dialog = new TextInputDialog("32")
    dialog.setTitle("Image Size")
    dialog.setHeaderText("What should be the image size? (#rows)")
    val sizeStr = dialog.showAndWait().toScala
    sizeStr.map(str => Try(str.toInt).toOption.filter(_ > 0).getOrElse(32))
  }
}
