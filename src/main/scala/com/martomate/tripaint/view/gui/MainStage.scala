package com.martomate.tripaint.view.gui

import java.io.File

import com.martomate.tripaint.control.{TriPaintController, TriPaintView}
import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.storage.ImageStorage
import com.martomate.tripaint.view.EditMode
import com.martomate.tripaint.view.image.ImagePane
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout.{AnchorPane, BorderPane, VBox}
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import scala.util.Try

class MainStage extends PrimaryStage with TriPaintView {
  private val controls: TriPaintController = new TriPaintController(this)
  val imageDisplay: ImagePane = new ImagePane(controls.imageGrid)

  private val menuBar: TheMenuBar  = new TheMenuBar(controls)
  private val toolBar: TheToolBar  = new TheToolBar(controls)
  private val toolBox: ToolBox     = new ToolBox
  private val imageTabs: ImageTabs = new ImageTabs(controls)
  private val colorBox: VBox       = makeColorBox()

  title = "TriPaint"
  onCloseRequest = e => {
    if (!controls.do_exit()) e.consume()
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

  override def askForSaveFile(image: ImageContent): Option[File] = {
    val chooser = new FileChooser
    chooser.title = "Save file"
    chooser.extensionFilters.add(new ExtensionFilter("PNG", "*.png"))
    Option(chooser.showSaveDialog(this))
  }

  override def askForFileToOpen(): Option[File] = {
    val chooser = new FileChooser
    chooser.title = "Open file"
    Option(chooser.showOpenDialog(this))
  }

  override def askForWhereToPutImage(): Option[(Int, Int)] = {
    DialogUtils.askForWhereToPutImage()
  }

  override def askForBlurRadius(): Option[Int] = {
    DialogUtils.getValueFromDialog[Int](
      controls.imagePool,
      controls.imageGrid.selectedImages,
      "Blur images",
      "How much should the images be blurred?",
      "Radius:",
      DialogUtils.uintRestriction,
      str => Try(str.toInt).getOrElse(0)
    )
  }

  override def askForMotionBlurRadius(): Option[Int] = {
    DialogUtils.getValueFromDialog[Int](
      controls.imagePool,
      controls.imageGrid.selectedImages,
      "Motion blur images",
      "How much should the images be motion blurred?",
      "Radius:",
      DialogUtils.uintRestriction,
      str => Try(str.toInt).getOrElse(0)
    )
  }

  override def askForRandomNoiseColors(): Option[(Color, Color)] = {
    val images = controls.imageGrid.selectedImages
    val loColorPicker = new ColorPicker(Color.Black)
    val hiColorPicker = new ColorPicker(Color.White)
    import DialogUtils._
    getValueFromCustomDialog[(Color, Color)](
      title = "Fill images randomly",
      headerText = "Which color-range should be used?",
      graphic = DialogUtils.makeImagePreviewList(images.map(_.content), controls.imagePool),

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
    alert.graphic = DialogUtils.makeImagePreviewList(images, controls.imagePool)

    alert.buttonTypes = Seq(
      new ButtonType("Save", ButtonData.Yes),
      new ButtonType("Don't save", ButtonData.No),
      new ButtonType("Cancel", ButtonData.CancelClose)
    )
    alert
  }

  override def askForOffset(): Option[(Int, Int)] = {
    DialogUtils.askForOffset()
  }

  override def shouldReplaceImage(currentImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Option[Boolean] = {
    val tri1 = controls.imageGrid.images.find(_.content.storage == newImage).orNull
    val tri2 = controls.imageGrid.images.find(_.content.storage == currentImage).orNull
    val alert = new Alert(AlertType.Confirmation)
    alert.title = "Collision"
    alert.headerText = "The image already exists on the screen. Which one should be used?"
    alert.graphic = DialogUtils.makeImagePreviewList(Seq(tri1.content, tri2.content), controls.imagePool)

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
