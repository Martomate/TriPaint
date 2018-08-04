package com.martomate.tripaint.control

import com.martomate.tripaint.image._
import com.martomate.tripaint.image.coords.TriImageCoords
import com.martomate.tripaint.image.effects._
import com.martomate.tripaint.image.format.SimpleStorageFormat
import com.martomate.tripaint.image.graphics.TriImage
import com.martomate.tripaint.image.pool.{ImagePool, ImagePoolImpl}
import com.martomate.tripaint.image.save.{ImageSaver, ImageSaverToFile}
import com.martomate.tripaint.image.storage._
import com.martomate.tripaint.TriPaintView
import com.martomate.tripaint.image.content.{ImageChangeTracker, ImageContent}
import com.martomate.tripaint.image.grid.{ImageGrid, ImageGridImplOld}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.paint.Color

import scala.util.{Failure, Success}

class TriPaintController(view: TriPaintView) {
  val imageGrid: ImageGrid = new ImageGridImplOld(32)
  val imagePool: ImagePool = new ImagePoolImpl(ImageStorageImpl, view)
  val imageSaver: ImageSaver = new ImageSaverToFile(new SimpleStorageFormat)

  def addImage(newImage: TriImage): Unit = {
    if (newImage != null) {
      imageGrid(newImage.content.coords) = newImage

      imageGrid.selectImage(newImage, replace = true)
    }
  }

  def saveBeforeClosing(images: TriImage*): Option[Boolean] = {
    view.askSaveBeforeClosing(images)
  }

  def do_exit(): Boolean = {
    imageGrid.images.filter(_.changed) match {
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

  def save(images: TriImage*): Boolean = images.filter(im => !imagePool.save(im.content.storage, imageSaver)).forall(im => imagePool.save(im.content.storage, imageSaver) || saveAs(im))

  def saveAs(image: TriImage): Boolean = {
    view.askForSaveFile(image) match {
      case Some(file) =>
        if (imagePool.move(image.content.storage, SaveLocation(file))) {
          val saved = imagePool.save(image.content.storage, imageSaver)
          if (!saved) println("Image could not be saved!!")
          saved
        } else false
      case None =>
        false
    }
  }

  val New: MenuBarAction = MenuBarAction.apply("New", "new", new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)) {
    view.askForWhereToPutImage() match {
      case Some((x, y)) =>
        addImage(TriImage(
          makeImageContent(TriImageCoords(x, y), imagePool.fromBGColor(new Color(view.imageDisplay.secondaryColor()), imageGrid.imageSize)),
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
      val offset = view.askForOffset().getOrElse(0, 0)

      imagePool.fromFile(SaveLocation(file, offset), imageSize) match {
        case Success(storage) =>
          view.askForWhereToPutImage() foreach { coords =>
            val image = TriImage.apply(makeImageContent(TriImageCoords(coords._1, coords._2), storage), view.imageDisplay)
            addImage(image)
          }
        case Failure(exc) =>
          exc.printStackTrace()
      }
    }
  }

  private def makeImageContent(coords: TriImageCoords, storage: ImageStorage) = {
    new ImageContent(coords, new ImageChangeTracker(storage, imagePool, imageSaver))
  }

  val Save: MenuBarAction = MenuBarAction.apply("Save", "save", new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)) {
    save(imageGrid.selectedImages.filter(_.changed): _*)
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
