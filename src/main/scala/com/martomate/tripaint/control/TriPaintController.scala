package com.martomate.tripaint.control

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.content.{ImageChangeTrackerImpl, ImageContent}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.effects._
import com.martomate.tripaint.model.format.SimpleStorageFormat
import com.martomate.tripaint.model.pool.{ImagePool, ImagePoolImpl, ImageSaveCollisionHandler}
import com.martomate.tripaint.model.save.{ImageSaver, ImageSaverToFile}
import com.martomate.tripaint.model.storage._
import com.martomate.tripaint.view.image.grid.{ImageGrid, ImageGridImplOld}
import com.martomate.tripaint.view.{TriPaintView, TriPaintViewListener}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.paint.Color

import scala.util.{Failure, Success}

class TriPaintModel(imageSize: Int, saveCollisionHandler: ImageSaveCollisionHandler) {
  val imageGrid: ImageGrid = new ImageGridImplOld(imageSize)//TODO: move from model to view, or split it
  val imagePool: ImagePool = new ImagePoolImpl(ImageStorageImpl, saveCollisionHandler)
  val imageSaver: ImageSaver = new ImageSaverToFile(new SimpleStorageFormat)
}

class TriPaintController(view: TriPaintView) extends TriPaintViewListener {
  view.addListener(this)

  val model: TriPaintModel = new TriPaintModel(view.askForImageSize().getOrElse(32), view)

  private def addImage(newImage: ImageContent): Unit = {
    if (newImage != null) {
      model.imageGrid(newImage.coords) = newImage
    }
  }

  def removeImageAt(coords: TriImageCoords): Unit = model.imageGrid -= coords

  def saveBeforeClosing(images: ImageContent*): Option[Boolean] = {
    view.askSaveBeforeClosing(images)
  }

  def do_exit(): Boolean = {
    allImages.filter(_.changeTracker.changed) match {
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

  private def allImages: Seq[ImageContent] = {
    model.imageGrid.images
  }

  def save(images: ImageContent*): Boolean = images.filter(im => !model.imagePool.save(im.storage, model.imageSaver)).forall(im => model.imagePool.save(im.storage, model.imageSaver) || saveAs(im))

  def saveAs(image: ImageContent): Boolean = {
    view.askForSaveFile(image) match {
      case Some(file) =>
        if (model.imagePool.move(image.storage, SaveLocation(file))) {
          val saved = model.imagePool.save(image.storage, model.imageSaver)
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
        addImage(makeImageContent(TriImageCoords(x, y), model.imagePool.fromBGColor(new Color(view.imageDisplay.colors.secondaryColor()), model.imageGrid.imageSize)))
      case _ =>
    }
  }

  val Open: MenuBarAction = MenuBarAction.apply("Open", "open", new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)) {
    view.askForFileToOpen() foreach { file =>
      val imageSize = model.imageGrid.imageSize
      val offset = view.askForOffset().getOrElse(0, 0)

      model.imagePool.fromFile(SaveLocation(file, offset), imageSize) match {
        case Success(storage) =>
          view.askForWhereToPutImage() foreach { coords =>
            val image = makeImageContent(TriImageCoords(coords._1, coords._2), storage)
            addImage(image)
          }
        case Failure(exc) =>
          exc.printStackTrace()
      }
    }
  }

  val OpenHexagon: MenuBarAction = MenuBarAction.apply("Open hexagon") {
    def coordOffset(idx: Int): (Int, Int) = {
      idx match {
        case 0 => (0, 0)
        case 1 => (-1, 0)
        case 2 => (-2, 0)
        case 3 => (-1, -1)
        case 4 => (0, -1)
        case 5 => (1, -1)
      }
    }

    view.askForFileToOpen() foreach { file =>
      val imageSize = model.imageGrid.imageSize
      val offset = view.askForOffset().getOrElse(0, 0)

      view.askForWhereToPutImage() foreach { coords =>
        for (idx <- 0 until 6) {
          model.imagePool.fromFile(SaveLocation(file, (offset._1 + idx * imageSize, offset._2)), imageSize) match {
            case Success(storage) =>
              val off = coordOffset(idx)
              val imageCoords = TriImageCoords(coords._1 + off._1, coords._2 + off._2)
              val image = makeImageContent(imageCoords, storage)
              addImage(image)
            case Failure(exc) =>
              exc.printStackTrace()
          }
        }
      }
    }
  }

  private def makeImageContent(coords: TriImageCoords, storage: ImageStorage) = {
    new ImageContent(coords, new ImageChangeTrackerImpl(storage, model.imagePool, model.imageSaver))
  }

  val Save: MenuBarAction = MenuBarAction.apply("Save", "save", new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)) {
    save(allSelectedImages.filter(_.changeTracker.changed): _*)
  }

  private def allSelectedImages: Seq[ImageContent] = {
    model.imageGrid.selectedImages
  }

  val SaveAs: MenuBarAction = MenuBarAction.apply("Save As", accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown, KeyCombination.ShiftDown)) {
    allSelectedImages.foreach(im => saveAs(im))
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
/*    val images = model.imageGrid.selectedImages
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

  private def applyEffect(effect: Effect): Unit = {
    allSelectedImages.foreach(_.applyEffect(effect))
  }

  val Blur: MenuBarAction = MenuBarAction.apply("Blur") {
    view.askForBlurRadius() foreach { radius =>
      applyEffect(new BlurEffect(radius))
    }
  }

  val MotionBlur: MenuBarAction = MenuBarAction.apply("Motion blur") {
    view.askForMotionBlurRadius() foreach { radius =>
      applyEffect(new MotionBlurEffect(radius))
    }
  }

  val RandomNoise: MenuBarAction = MenuBarAction.apply("Random noise") {
    view.askForRandomNoiseColors() foreach { case (lo, hi) =>
      applyEffect(new RandomNoiseEffect(lo, hi))
    }
  }

  val Scramble: MenuBarAction = MenuBarAction.apply("Scramble") {
    applyEffect(ScrambleEffect)
  }
}
