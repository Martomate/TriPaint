package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.effects.{BlurEffect, MotionBlurEffect, RandomNoiseEffect}
import com.martomate.tripaint.model.image.{ImageStorage, SaveLocation}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.format.{RecursiveStorageFormat, SimpleStorageFormat}
import com.martomate.tripaint.view.image.ImageGridPane
import com.martomate.tripaint.view.*
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control.*
import scalafx.scene.layout.{AnchorPane, BorderPane, TilePane, VBox}
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.File
import scala.util.Try

class MainStage(controls: TriPaintViewListener, model: TriPaintModel)
    extends PrimaryStage
    with TriPaintView {
  private val imageDisplay: ImageGridPane = new ImageGridPane(model.imageGrid)

  private val buttons = new MainStageButtons(controls)

  private val menuBar: MenuBar = TheMenuBar.create(buttons)
  private val toolBar: ToolBar = TheToolBar.create(buttons)
  private val toolBox: TilePane = ToolBox.create(EditMode.modes)
  private val imageTabs: TilePane =
    ImageTabs.fromImagePool(model.imageGrid, model.imagePool, controls.requestImageRemoval)
  private val colorBox: VBox = makeColorBox()

  private var currentFolder: Option[File] = None

  title = "TriPaint"
  onCloseRequest = e => {
    if (!controls.requestExit()) e.consume()
  }
  scene = new Scene(720, 720) {
    delegate.getStylesheets.add(getClass.getResource("/styles/application.css").toExternalForm)
    root = new BorderPane {
      top = new VBox(menuBar, toolBar)
      center = new AnchorPane {
        AnchorPane.setAnchors(imageDisplay, 0, 0, 0, 0)
        imageDisplay.clip === this.clip

        AnchorPane.setLeftAnchor(toolBox, 0)
        AnchorPane.setTopAnchor(toolBox, 0)

        AnchorPane.setLeftAnchor(colorBox, 10)
        AnchorPane.setBottomAnchor(colorBox, 10)

        AnchorPane.setRightAnchor(imageTabs, 10)
        AnchorPane.setTopAnchor(imageTabs, 10)
        AnchorPane.setBottomAnchor(imageTabs, 10)
        this.children = Seq(imageDisplay, toolBox, colorBox, imageTabs)
      }
    }
  }

  EditMode.modes
    .filter(_.shortCut != null)
    .foreach(m => scene().getAccelerators.put(m.shortCut, () => m.toolboxButton.fire()))

  private def makeColorBox() = {
    // overlay and imageDisplay
    val colorPicker1 = new ColorPicker(new Color(imageDisplay.colors.primaryColor()))
    val colorPicker2 = new ColorPicker(new Color(imageDisplay.colors.secondaryColor()))

    imageDisplay.colors.primaryColor <==> colorPicker1.value
    imageDisplay.colors.secondaryColor <==> colorPicker2.value

    new VBox(
      new Label("Primary color:"),
      colorPicker1,
      new Label("Secondary color:"),
      colorPicker2
    )
  }

  override def backgroundColor: Color = new Color(imageDisplay.colors.secondaryColor())

  override def askForSaveFile(image: ImageContent): Option[File] = {
    val chooser = new FileChooser
    currentFolder.foreach(chooser.initialDirectory = _)
    chooser.title = "Save file"
    chooser.extensionFilters.add(new ExtensionFilter("PNG", "*.png"))
    val result = Option(chooser.showSaveDialog(this))
    result.foreach(r => currentFolder = Some(r.getParentFile))
    result
  }

  override def askForFileSaveSettings(file: File, image: ImageContent): Option[FileSaveSettings] = {
    AskForFileSaveSettingsDialog.askForFileSaveSettings(
      image.storage,
      file,
      Seq(
        new SimpleStorageFormat -> "Simple format (old)",
        new RecursiveStorageFormat -> "Recursive format (new)"
      ),
      1
    )
  }

  override def askForFileToOpen(): Option[File] = {
    val chooser = new FileChooser
    chooser.title = "Open file"
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
    val loColorPicker = new ColorPicker(Color.Black)
    val hiColorPicker = new ColorPicker(Color.White)
    import DialogUtils._

    val (previewPane, updatePreview) = DialogUtils.makeImagePreviewList(images, model.imagePool)

    def updatePreviewFromInputs(): Unit =
      val lo = new Color(loColorPicker.value())
      val hi = new Color(hiColorPicker.value())
      updatePreview(grid => new RandomNoiseEffect(lo, hi).action(selectedImagesCoords, grid))

    loColorPicker.value.onChange((_, _, _) => updatePreviewFromInputs())
    hiColorPicker.value.onChange((_, _, _) => updatePreviewFromInputs())

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
          Try((new Color(loColorPicker.value()), new Color(hiColorPicker.value()))).getOrElse(null)
        case _ => null
      },
      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }

  override def askSaveBeforeClosing(images: Seq[ImageContent]): Option[Boolean] = {
    saveBeforeClosingAlert(images).showAndWait().map(_.buttonData) flatMap {
      case ButtonData.Yes => Some(true)
      case ButtonData.No  => Some(false)
      case _              => None
    }
  }

  private def saveBeforeClosingAlert(images: Seq[ImageContent]): Alert = {
    val (previewPane, _) = DialogUtils.makeImagePreviewList(images, model.imagePool)

    val alert = new Alert(AlertType.Confirmation)
    alert.title = "Save before closing?"
    alert.headerText = "Do you want to save " + (if (images.size == 1) "this image"
                                                 else "these images") + " before closing the tab?"
    alert.graphic = previewPane

    alert.buttonTypes = Seq(
      new ButtonType("Save", ButtonData.Yes),
      new ButtonType("Don't save", ButtonData.No),
      new ButtonType("Cancel", ButtonData.CancelClose)
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
        new SimpleStorageFormat -> "Simple format (old)",
        new RecursiveStorageFormat -> "Recursive format (new)"
      ),
      1,
      model.fileSystem
    )
  }

  override def shouldReplaceImage(
      currentImage: ImageStorage,
      newImage: ImageStorage,
      location: SaveLocation
  ): Option[Boolean] = {
    val tri1 = model.imageGrid.images.find(_.storage == newImage).orNull
    val tri2 = model.imageGrid.images.find(_.storage == currentImage).orNull
    val (previewPane, _) = DialogUtils.makeImagePreviewList(Seq(tri1, tri2), model.imagePool)

    val alert = new Alert(AlertType.Confirmation)
    alert.title = "Collision"
    alert.headerText = "The image already exists on the screen. Which one should be used?"
    alert.graphic = previewPane

    alert.buttonTypes = Seq(
      new ButtonType("Left", ButtonData.Yes),
      new ButtonType("Right", ButtonData.No),
      new ButtonType("Cancel", ButtonData.CancelClose)
    )
    alert.showAndWait().map(_.buttonData == ButtonData.Yes)
  }

  override def askForImageSize(): Option[Int] = {
    val dialog = new TextInputDialog("32")
    dialog.title = "Image Size"
    dialog.headerText = "What should be the image size? (#rows)"
    val sizeStr = dialog.showAndWait()
    sizeStr.map(str => Try(str.toInt).toOption.filter(_ > 0).getOrElse(32))
  }
}
