package com.martomate.tripaint.view.gui

import java.io.File

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.storage.ImageStorage
import com.martomate.tripaint.model.{SaveLocation, TriPaintModel}
import com.martomate.tripaint.view.image.ImagePane
import com.martomate.tripaint.view.{EditMode, MenuBarAction, TriPaintView, TriPaintViewListener}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.layout.{AnchorPane, BorderPane, VBox}
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import scala.util.Try

class MainStageButtons(control: TriPaintViewListener) {
  val New: MenuBarAction = MenuBarAction("New", "new", new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)) (control.action_new())
  val Open: MenuBarAction = MenuBarAction("Open", "open", new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)) (control.action_open())
  val OpenHexagon: MenuBarAction = MenuBarAction("Open hexagon", "open_hexagon", new KeyCodeCombination(KeyCode.H, KeyCombination.ControlDown)) (control.action_openHexagon())
  val Save: MenuBarAction = MenuBarAction("Save", "save", new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)) (control.action_save())
  val SaveAs: MenuBarAction = MenuBarAction("Save As", accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown, KeyCombination.ShiftDown)) (control.action_saveAs())
  val Exit: MenuBarAction = MenuBarAction("Exit") (control.action_exit())
  val Undo: MenuBarAction = MenuBarAction("Undo", "undo", new KeyCodeCombination(KeyCode.Z, KeyCombination.ControlDown)) (control.action_undo())
  val Redo: MenuBarAction = MenuBarAction("Redo", "redo", new KeyCodeCombination(KeyCode.Y, KeyCombination.ControlDown)) (control.action_redo())
  val Cut: MenuBarAction = MenuBarAction("Cut", "cut") ()
  val Copy: MenuBarAction = MenuBarAction("Copy", "copy") ()
  val Paste: MenuBarAction = MenuBarAction("Paste", "paste") ()
  val Move: MenuBarAction = MenuBarAction("Move", "move") ()
  val Scale: MenuBarAction = MenuBarAction("Scale", "scale") ()
  val Rotate: MenuBarAction = MenuBarAction("Rotate", "rotate") ()
  val Blur: MenuBarAction = MenuBarAction("Blur") (control.action_blur())
  val MotionBlur: MenuBarAction = MenuBarAction("Motion blur") (control.action_motionBlur())
  val RandomNoise: MenuBarAction = MenuBarAction("Random noise") (control.action_randomNoise())
  val Scramble: MenuBarAction = MenuBarAction("Scramble") (control.action_scramble())
}

class MainStage(controls: TriPaintViewListener, model: TriPaintModel) extends PrimaryStage with TriPaintView {
  private val imageDisplay: ImagePane = new ImagePane(model.imageGrid)

  private val buttons = new MainStageButtons(controls)

  private val menuBar: TheMenuBar  = new TheMenuBar(buttons)
  private val toolBar: TheToolBar  = new TheToolBar(buttons)
  private val toolBox: ToolBox     = new ToolBox
  private val imageTabs: ImageTabs = new ImageTabs(controls, model)
  private val colorBox: VBox       = makeColorBox()

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
    .foreach(m => scene().getAccelerators.put(m.shortCut, () => m.toolboxButton.fire))

  private def makeColorBox() = {
    //overlay and imageDisplay
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
    chooser.title = "Save file"
    chooser.extensionFilters.add(new ExtensionFilter("PNG", "*.png"))
    Option(chooser.showSaveDialog(this))
  }

  override def askForFileToOpen(): Option[File] = {
    val chooser = new FileChooser
    currentFolder.foreach(chooser.initialDirectory = _)
    chooser.title = "Open file"
    val result = Option(chooser.showOpenDialog(this))
    result.foreach(r => currentFolder = Some(r.getParentFile))
    result
  }

  override def askForWhereToPutImage(): Option[(Int, Int)] = {
    DialogUtils.askForWhereToPutImage()
  }

  override def askForBlurRadius(): Option[Int] = {
    DialogUtils.getValueFromDialog[Int](
      model.imagePool,
      model.imageGrid.selectedImages,
      "Blur images",
      "How much should the images be blurred?",
      "Radius:",
      DialogUtils.uintRestriction,
      str => Try(str.toInt).getOrElse(0)
    )
  }

  override def askForMotionBlurRadius(): Option[Int] = {
    DialogUtils.getValueFromDialog[Int](
      model.imagePool,
      model.imageGrid.selectedImages,
      "Motion blur images",
      "How much should the images be motion blurred?",
      "Radius:",
      DialogUtils.uintRestriction,
      str => Try(str.toInt).getOrElse(0)
    )
  }

  override def askForRandomNoiseColors(): Option[(Color, Color)] = {
    val images = model.imageGrid.selectedImages
    val loColorPicker = new ColorPicker(Color.Black)
    val hiColorPicker = new ColorPicker(Color.White)
    import DialogUtils._
    getValueFromCustomDialog[(Color, Color)](
      title = "Fill images randomly",
      headerText = "Which color-range should be used?",
      graphic = DialogUtils.makeImagePreviewList(images, model.imagePool),

      content = Seq(makeGridPane(Seq(
        Seq(new Label("Minimum color:"), loColorPicker),
        Seq(new Label("Maximum color:"), hiColorPicker)
      ))),

      resultConverter = {
        case ButtonType.OK => Try((new Color(loColorPicker.value()), new Color(hiColorPicker.value()))).getOrElse(null)
        case _ => null
      },

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }

  override def askSaveBeforeClosing(images: Seq[ImageContent]): Option[Boolean] = {
    saveBeforeClosingAlert(images).showAndWait().map(_.buttonData) flatMap {
      case ButtonData.Yes => Some(true)
      case ButtonData.No => Some(false)
      case _ => None
    }
  }

  private def saveBeforeClosingAlert(images: Seq[ImageContent]): Alert = {
    val alert = new Alert(AlertType.Confirmation)
    alert.title = "Save before closing?"
    alert.headerText = "Do you want to save " + (if (images.size == 1) "this image" else "these images") + " before closing the tab?"
    alert.graphic = DialogUtils.makeImagePreviewList(images, model.imagePool)

    alert.buttonTypes = Seq(
      new ButtonType("Save", ButtonData.Yes),
      new ButtonType("Don't save", ButtonData.No),
      new ButtonType("Cancel", ButtonData.CancelClose)
    )
    alert
  }

  override def askForOffset(file: File, width: Int, height: Int): Option[(Int, Int)] = {
    DialogUtils.askForOffset(file, width, height)
  }

  override def shouldReplaceImage(currentImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Option[Boolean] = {
    val tri1 = model.imageGrid.images.find(_.storage == newImage).orNull
    val tri2 = model.imageGrid.images.find(_.storage == currentImage).orNull
    val alert = new Alert(AlertType.Confirmation)
    alert.title = "Collision"
    alert.headerText = "The image already exists on the screen. Which one should be used?"
    alert.graphic = DialogUtils.makeImagePreviewList(Seq(tri1, tri2), model.imagePool)

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
