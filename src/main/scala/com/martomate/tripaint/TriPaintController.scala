package com.martomate.tripaint

import com.martomate.tripaint.image.effects._
import com.martomate.tripaint.image.storage.{ImageSourceImpl, ImageStorage, SaveLocation}
import com.martomate.tripaint.image.{ImageGrid, ImageGridImplOld, TriImage, TriImageCoords}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.paint.Color

import scala.util.{Failure, Success}

class TriPaintController(view: TriPaintView) {
  val imageGrid: ImageGrid = new ImageGridImplOld(32)

  def addImage(newImage: TriImage): Unit = {
    if (newImage != null) {
      imageGrid(newImage.coords) = newImage

      imageGrid.selectImage(newImage, replace = true)
    }
  }

  def saveBeforeClosing(images: TriImage*): Option[Boolean] = {
    view.askSaveBeforeClosing(images)
  }

  def do_exit(): Boolean = {
    imageGrid.images.filter(_.hasChanged) match {
      case Seq() => true
      case images =>
        saveBeforeClosing(images: _*) match {
          case Some(shouldSave) =>
            if (shouldSave) save(images: _*)
            else true
          case None => false
        }
    }
  }

  private def save(images: TriImage*): Boolean = images.filter(!_.save).forall(im => im.storage.save || saveAs(im))

  def saveAs(image: TriImage): Boolean = {
    view.askForSaveFile(image) match {
      case Some(file) =>
        image.setSaveLocation(SaveLocation(file, None))
        val saved = image.save
        if (!saved) println("Image could not be saved!!")
        saved
      case None =>
        false
    }
  }

  val New: MenuBarAction = MenuBarAction.apply("New", "new", new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)) {
    view.askForWhereToPutImage() match {
      case Some((x, y)) =>
        addImage(TriImage(
          TriImageCoords(x, y),
          ImageStorage.unboundImage(imageGrid.imageSize, new Color(view.imageDisplay.secondaryColor())),
          view.imageDisplay
        ))
      case _ =>
    }
  }

  val NewComp: MenuBarAction = MenuBarAction.apply("New composition", accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown, KeyCombination.ShiftDown)) {
/*    val dialog = new TextInputDialog
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
    }*/
  }

  val Open: MenuBarAction = MenuBarAction.apply("Open", "open", new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)) {
    view.askForFileToOpen() foreach { file =>
      val imageSize = imageGrid.imageSize

      ImageSourceImpl.fromFile(file) match {
        case Success(source) =>
          val offset = if (source.width != imageSize || source.height != imageSize)
            view.askForOffset()
          else Some(0, 0)

          if (offset.isDefined) {
            view.askForWhereToPutImage() foreach { coords =>
              TriImage.loadFromSource(TriImageCoords(coords._1, coords._2), source, view.imageDisplay, offset, imageSize) foreach { image =>
                addImage(image)
              }
            }
          }
        case Failure(exc) =>
          exc.printStackTrace()
      }
    }
  }

  val Save: MenuBarAction = MenuBarAction.apply("Save", "save", new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)) {
    save(imageGrid.selectedImages.filter(_.hasChanged): _*)
  }

  val SaveAs: MenuBarAction = MenuBarAction.apply("Save As", accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown, KeyCombination.ShiftDown)) {
    imageGrid.selectedImages.foreach(saveAs)
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
/*    val images = imageGrid.selectedImages
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
      case Some((h, v)) => //images.foreach(_.move(h, v))
      case _ =>
    }*/
  }

  val Scale: MenuBarAction = MenuBarAction.apply("Scale", "scale") {
/*    makeTextInputDialog[Double](
      "Scale images",
      "How much should the images be scaled?",
      "Scale factor:",
      DialogUtils.doubleRestriction,
      str => Try(str.toDouble).getOrElse(0d),
      (im, sc) => ()//im scale sc
    )*/
  }

  val Rotate: MenuBarAction = MenuBarAction.apply("Rotate", "rotate") {
/*    makeTextInputDialog[Double](
      "Rotate images",
      "How much should the images be rotated (degrees)?",
      "Angle:",
      DialogUtils.doubleRestriction,
      str => Try(str.toDouble).getOrElse(0d),
      (im, rt) => ()//im rotate rt
    )*/
  }

  val Fit: MenuBarAction = MenuBarAction.apply("Fit") {
    ???
  }

  val Blur: MenuBarAction = MenuBarAction.apply("Blur") {
    view.askForBlurRadius() foreach { radius =>
      val effect = new BlurEffect(radius)
      imageGrid.selectedImages.foreach(_.applyEffect(effect))
    }
  }

  val MotionBlur: MenuBarAction = MenuBarAction.apply("Motion blur") {
    view.askForMotionBlurRadius() foreach { radius =>
      val effect = new MotionBlurEffect(radius)
      imageGrid.selectedImages.foreach(_.applyEffect(effect))
    }
  }

  val RandomNoise: MenuBarAction = MenuBarAction.apply("Random noise") {
    view.askForRandomNoiseColors() foreach { case (lo, hi) =>
      val effect = new RandomNoiseEffect(lo, hi)
      imageGrid.selectedImages.foreach(_.applyEffect(effect))
    }
  }

  val Scramble: MenuBarAction = MenuBarAction.apply("Scramble") {
    imageGrid.selectedImages.foreach(_.applyEffect(ScrambleEffect))
  }
}
