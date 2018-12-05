package com.martomate.tripaint.control

import com.martomate.tripaint.model.content.{ImageChangeTrackerImpl, ImageContent}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.effects._
import com.martomate.tripaint.model.storage._
import com.martomate.tripaint.model.{SaveLocation, TriPaintModel}
import com.martomate.tripaint.view.gui.MainStage
import com.martomate.tripaint.view.{TriPaintView, TriPaintViewListener}
import scalafx.scene.paint.Color

import scala.util.{Failure, Success}

class TriPaintController(val model: TriPaintModel) extends TriPaintViewListener {
  val view: TriPaintView = new MainStage(this, model)

  model.imageGrid.setImageSizeIfEmpty(view.askForImageSize().getOrElse(32))

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
        if (model.imagePool.move(image.storage, SaveLocation(file))(view)) {
          val saved = model.imagePool.save(image.storage, model.imageSaver)
          if (!saved) println("Image could not be saved!!")
          saved
        } else false
      case None =>
        false
    }
  }

  private def makeImageContent(coords: TriImageCoords, storage: ImageStorage) = {
    new ImageContent(coords, new ImageChangeTrackerImpl(storage, model.imagePool, model.imageSaver))
  }

  private def allSelectedImages: Seq[ImageContent] = {
    model.imageGrid.selectedImages
  }

  override def action_new(): Unit = {
    view.askForWhereToPutImage() match {
      case Some((x, y)) =>
        addImage(makeImageContent(TriImageCoords(x, y), model.imagePool.fromBGColor(new Color(view.backgroundColor), model.imageGrid.imageSize)))
      case _ =>
    }
  }

  override def action_open(): Unit = {
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

  override def action_openHexagon(): Unit = {
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

  override def action_save(): Unit = {
    save(allSelectedImages.filter(_.changeTracker.changed): _*)
  }

  override def action_saveAs(): Unit = {
    allSelectedImages.foreach(im => saveAs(im))
  }

  override def action_exit(): Unit = if (do_exit()) view.close()

  override def action_undo(): Unit = model.imageGrid.images.foreach(_.undo())

  override def action_redo(): Unit = model.imageGrid.images.foreach(_.redo())

  private def applyEffect(effect: Effect): Unit = {
    allSelectedImages.foreach(_.applyEffect(effect))
  }

  override def action_blur(): Unit = {
    view.askForBlurRadius() foreach { radius =>
      applyEffect(new BlurEffect(radius))
    }
  }

  override def action_motionBlur(): Unit = {
    view.askForMotionBlurRadius() foreach { radius =>
      applyEffect(new MotionBlurEffect(radius))
    }
  }

  override def action_randomNoise(): Unit = {
    view.askForRandomNoiseColors() foreach { case (lo, hi) =>
      applyEffect(new RandomNoiseEffect(lo, hi))
    }
  }

  override def action_scramble(): Unit = applyEffect(ScrambleEffect)

  override def requestExit(): Boolean = {
    do_exit()
  }

  override def requestImageRemoval(image: ImageContent): Unit = {
    var abortRemoval = false
    if (image.changeTracker.changed) {
      saveBeforeClosing(image) match {
        case Some(shouldSave) =>
          if (shouldSave && !save(image)) abortRemoval = true
        case None => abortRemoval = true
      }
    }

    if (!abortRemoval) {
      removeImageAt(image.coords)
    }
  }
}
