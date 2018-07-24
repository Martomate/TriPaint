package com.martomate.tripaint

import com.martomate.tripaint.image.effects._
import com.martomate.tripaint.image.storage.{ImageSourceImpl, ImageStorage, SaveLocation}
import com.martomate.tripaint.image.{TriImage, TriImageCoords}
import scalafx.scene.SnapshotParameters
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.image.ImageView
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import scala.util.{Failure, Success, Try}

trait TriPaintView {
  def imageDisplay: ImagePane
  def addImage(newImage: TriImage): Unit
  def close(): Unit
}

class TriPaintController {
  val view: TriPaintView = TriPaint

  private def makeTextInputDialog[T](title: String, headerText: String, contentText: String, restriction: String => Boolean, stringToValue: String => T, action: (TriImage, T) => Unit): TextInputDialog = {
    val images = view.imageDisplay.getSelectedImages
    val dialog = new TextInputDialog
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = makeImagePreviewList(images)
    DialogUtils.restrictTextField(dialog.editor, restriction)
    dialog.showAndWait match {
      case Some(str) =>
        val num = stringToValue(str)
        images.foreach(action(_, num))
      case None =>
    }
    dialog
  }

  private def makeImagePreviewList(images: Seq[TriImage]): ScrollPane = {
    val params = new SnapshotParameters
    params.fill = Color.Transparent

    val imageViews = images.map(im => {
      val view = new ImageView
      view.image = im.preview.snapshot(params, null)
      Tooltip.install(view.delegate, im.toolTip())
      view
    })
    val sp = new ScrollPane
    sp.maxWidth = TriImage.previewSize * 5
    sp.content = new HBox(children = imageViews: _*)
    sp.minViewportHeight = TriImage.previewSize * Math.sqrt(3) / 2
    sp
  }

  def saveBeforeClosing(images: TriImage*): Alert = {
    val alert = new Alert(AlertType.Confirmation)
    alert.title = "Save before closing?"
    alert.headerText = "Do you want to save " + (if (images.size == 1) "this image" else "these images") + " before closing the tab?"
    alert.graphic = makeImagePreviewList(images)

    alert.buttonTypes = Seq(
      new ButtonType("Save", ButtonData.Yes),
      new ButtonType("Don't save", ButtonData.No),
      new ButtonType("Cancel", ButtonData.CancelClose)
    )
    alert
  }

  def do_exit(): Boolean = {
    view.imageDisplay.getImages.filter(_.hasChanged) match {
      case Vector() => true
      case images =>
        saveBeforeClosing(images: _*).showAndWait match {
          case Some(t) => t.buttonData match {
            case ButtonData.Yes => save(images: _*)
            case ButtonData.No => true
            case _ => false
          }
          case None => false
        }
    }
  }

  private def save(images: TriImage*): Boolean = images.filter(!_.save).forall(im => im.storage.save || saveAs(im))

  def saveAs(image: TriImage): Boolean = {
    val chooser = new FileChooser
    chooser.title = "Save file"
    chooser.extensionFilters.add(new ExtensionFilter("PNG", "*.png"))
    val file = chooser.showSaveDialog(null)
    if (file != null) {
      image.setSaveLocation(SaveLocation(file, None))
      if (!image.save) {
        println("Image could not be saved!!")
        false
      } else true
    } else false
  }

  val New: MenuBarAction = MenuBarAction.apply("New", "new", new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)) {
    DialogUtils.askForWhereToPutImage() match {
      case Some((x, y)) =>
        view.addImage(TriImage(TriImageCoords(x, y),
          ImageStorage.unboundImage(view.imageDisplay.imageSize, new Color(view.imageDisplay.secondaryColor())), view.imageDisplay))
      case _ =>
    }
  }

  val NewComp: MenuBarAction = MenuBarAction.apply("New composition", accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown, KeyCombination.ShiftDown)) {
    val dialog = new TextInputDialog
    dialog.title = "New image composition"
    dialog.headerText = "Please enter number of images."
    dialog.contentText = "Number of images:"
    DialogUtils.restrictTextField(dialog.editor, DialogUtils.uintRestriction)

    dialog.showAndWait.foreach { result =>
      try {
        val num = result.toInt

      } catch {
        case _: Exception =>
      }
    }
  }

  val Open: MenuBarAction = MenuBarAction.apply("Open", "open", new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)) {
    val chooser = new FileChooser
    chooser.title = "Open file"
    val file = chooser.showOpenDialog(null)
    if (file != null) {
      val imageSize = view.imageDisplay.imageSize

      ImageSourceImpl.fromFile(file) match {
        case Success(source) =>
          val offset = if (source.width != imageSize || source.height != imageSize)
            DialogUtils.askForOffset()
          else Some(0, 0)

          if (offset.isDefined) {
            DialogUtils.askForWhereToPutImage() foreach { coords =>
              TriImage.loadFromSource(TriImageCoords(coords._1, coords._2), source, view.imageDisplay, offset, imageSize)
            }
          }
        case Failure(exc) =>
          exc.printStackTrace()
      }
    }
  }

  val Save: MenuBarAction = MenuBarAction.apply("Save", "save", new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)) {
    save(view.imageDisplay.getSelectedImages.filter(_.hasChanged): _*)
  }

  val SaveAs: MenuBarAction = MenuBarAction.apply("Save As", accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown, KeyCombination.ShiftDown)) {
    view.imageDisplay.getSelectedImages.foreach(saveAs)
  }

  val Exit: MenuBarAction = MenuBarAction.apply("Exit") {
    if (do_exit()) view.close()
  }

  val Undo: MenuBarAction = MenuBarAction.apply("Undo", "undo", new KeyCodeCombination(KeyCode.Z, KeyCombination.ControlDown)) {
    view.imageDisplay.undo
  }

  val Redo: MenuBarAction = MenuBarAction.apply("Redo", "redo", new KeyCodeCombination(KeyCode.Y, KeyCombination.ControlDown)) {
    view.imageDisplay.redo
  }

  val Cut: MenuBarAction = MenuBarAction.apply("Cut", "cut", new KeyCodeCombination(KeyCode.X, KeyCombination.ControlDown)) {
    ???
  }

  val Copy: MenuBarAction = MenuBarAction.apply("Copy", "copy", new KeyCodeCombination(KeyCode.C, KeyCombination.ControlDown)) {
    ???
  }

  val Paste: MenuBarAction = MenuBarAction.apply("Paste", "paste", new KeyCodeCombination(KeyCode.V, KeyCombination.ControlDown)) {
    ???
  }

  val Move: MenuBarAction = MenuBarAction.apply("Move", "move") {
    val images = view.imageDisplay.getSelectedImages
    val horizTextField = DialogUtils.doubleTF
    val vertTextField = DialogUtils.doubleTF
    import DialogUtils._
    showInputDialog[(Double, Double)](
      title = "Move images",
      headerText = "How far should the images move?",
      graphic = makeImagePreviewList(images),

      content = Seq(makeGridPane(Seq(
        Seq(new Label("Horizontal movement:"), horizTextField),
        Seq(new Label("Vertical movement:"), vertTextField)
      ))),

      resultConverter = {
        case ButtonType.OK => Try((horizTextField.text().toDouble, vertTextField.text().toDouble)).getOrElse(null)
        case _ => null
      },

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    ) match {
      case Some((h, v)) => images.foreach(_.move(h, v))
      case _ =>
    }
  }

  val Scale: MenuBarAction = MenuBarAction.apply("Scale", "scale") {
    makeTextInputDialog[Double](
      "Scale images",
      "How much should the images be scaled?",
      "Scale factor:",
      DialogUtils.doubleRestriction,
      str => Try(str.toDouble).getOrElse(0d),
      (im, sc) => im scale sc
    )
  }

  val Rotate: MenuBarAction = MenuBarAction.apply("Rotate", "rotate") {
    makeTextInputDialog[Double](
      "Rotate images",
      "How much should the images be rotated (degrees)?",
      "Angle:",
      DialogUtils.doubleRestriction,
      str => Try(str.toDouble).getOrElse(0d),
      (im, rt) => im rotate rt
    )
  }

  val Fit: MenuBarAction = MenuBarAction.apply("Fit") {
    ???
  }

  val Blur: MenuBarAction = MenuBarAction.apply("Blur") {
    makeTextInputDialog[Int](
      "Blur images",
      "How much should the images be blurred?",
      "Radius:",
      DialogUtils.uintRestriction,
      str => Try(str.toInt).getOrElse(0),
      (im, amt) => im.applyEffect(new BlurEffect(amt))
    )
  }

  val MotionBlur: MenuBarAction = MenuBarAction.apply("Motion blur") {
    makeTextInputDialog[Int](
      "Motionblur images",
      "How much should the images be motionblurred?",
      "Radius:",
      DialogUtils.uintRestriction,
      str => Try(str.toInt).getOrElse(0),
      (im, amt) => im.applyEffect(new MotionBlurEffect(amt))
    )
  }

  val PerlinNoise: MenuBarAction = MenuBarAction.apply("Perlin noise") {
    view.imageDisplay.getSelectedImages.foreach(_.applyEffect(PerlinNoiseEffect))
  }

  val RandomNoise: MenuBarAction = MenuBarAction.apply("Random noise") {
    val images = view.imageDisplay.getSelectedImages
    val loColorPicker = new ColorPicker(Color.Black)
    val hiColorPicker = new ColorPicker(Color.White)
    import DialogUtils._
    showInputDialog[(Color, Color)](
      title = "Fill images randomly",
      headerText = "Which color-range should be used?",
      graphic = makeImagePreviewList(images),

      content = Seq(makeGridPane(Seq(
        Seq(new Label("Minimum color:"), loColorPicker),
        Seq(new Label("Maximum color:"), hiColorPicker)
      ))),

      resultConverter = {
        case ButtonType.OK => Try((new Color(loColorPicker.value()), new Color(hiColorPicker.value()))).getOrElse(null)
        case _ => null
      },

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    ) match {
      case Some((lo, hi)) => images.foreach(_.applyEffect(new RandomNoiseEffect(lo, hi)))
      case _ =>
    }
  }

  val Scramble: MenuBarAction = MenuBarAction.apply("Scramble") {
    view.imageDisplay.getSelectedImages.foreach(_.applyEffect(ScrambleEffect))
  }
}
