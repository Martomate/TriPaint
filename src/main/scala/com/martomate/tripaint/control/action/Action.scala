package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.SaveInfo
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.view.{FileSaveSettings, TriPaintView}

abstract class Action {
  def perform(model: TriPaintModel, view: TriPaintView): Unit

  protected def addImage(model: TriPaintModel, newImage: ImageContent): Unit = {
    if (newImage != null) {
      model.imageGrid.set(newImage)
    }
  }

  protected def makeImageContent(model: TriPaintModel, coords: TriImageCoords, storage: ImageStorage): ImageContent = {
    val image = new ImageContent(coords, storage)
    model.imagePool.addListener(image)
    image
  }

  protected def allImages(model: TriPaintModel): Seq[ImageContent] = {
    model.imageGrid.images.toSeq
  }

  protected def allSelectedImages(model: TriPaintModel): Seq[ImageContent] = {
    model.imageGrid.selectedImages.toSeq
  }

  protected def save(model: TriPaintModel, view: TriPaintView, images: ImageContent*): Boolean = {
    images.filter(im => !model.imagePool.save(im.storage, model.imageSaver, model.fileSystem))
      .forall(im => model.imagePool.save(im.storage, model.imageSaver, model.fileSystem) || saveAs(model, view, im))
  }

  protected def saveAs(model: TriPaintModel, view: TriPaintView, image: ImageContent): Boolean = {
    view.askForSaveFile(image) flatMap { file =>
      view.askForFileSaveSettings(file, image) map {
        case FileSaveSettings(offset, format) =>
          if (model.imagePool.move(image.storage, SaveLocation(file, offset), SaveInfo(format))(view)) {
            val saved = model.imagePool.save(image.storage, model.imageSaver, model.fileSystem)
            if (!saved) println("Image could not be saved!!")
            saved
          } else false
      }
    } getOrElse false
  }

  protected def saveBeforeClosing(view: TriPaintView, images: ImageContent*): Option[Boolean] = {
    view.askSaveBeforeClosing(images)
  }
}
