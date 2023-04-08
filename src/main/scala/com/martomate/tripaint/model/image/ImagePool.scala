package com.martomate.tripaint.model.image

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.model.image.{ImageStorage, ImageUtils}
import com.martomate.tripaint.util.{EventDispatcher, InjectiveHashMap, InjectiveMap, Tracker}

import java.io.File
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(
      currentImage: ImageStorage,
      newImage: ImageStorage,
      location: ImagePool.SaveLocation
  ): Option[Boolean]
}

object ImagePool {
  enum Event:
    case ImageSaved(image: ImageStorage)
    case ImageReplaced(oldImage: ImageStorage, newImage: ImageStorage, location: SaveLocation)

  case class SaveInfo(format: StorageFormat)
  case class SaveLocation(file: File, offset: StorageCoords = StorageCoords(0, 0))
}

/** This class should keep track of Map[SaveLocation, ImageStorage] So if SaveAs then this class
  * should be called so that collisions can be detected and dealt with, like e.g. when you save A
  * into the same place as B the user should be asked which image to keep, and after that they will
  * share the same ImageStorage.
  */
class ImagePool {
  import ImagePool.{SaveLocation, SaveInfo}

  private val mapping: InjectiveMap[SaveLocation, ImageStorage] = new InjectiveHashMap
  private val saveInfo: mutable.Map[ImageStorage, SaveInfo] = mutable.Map.empty

  private val dispatcher = new EventDispatcher[ImagePool.Event]
  def trackChanges(tracker: Tracker[ImagePool.Event]): Unit = dispatcher.track(tracker)

  private def contains(saveLocation: SaveLocation): Boolean = mapping.containsLeft(saveLocation)
  private def get(saveLocation: SaveLocation): ImageStorage = mapping.getRight(saveLocation).orNull
  private def set(saveLocation: SaveLocation, imageStorage: ImageStorage): Unit =
    mapping.set(saveLocation, imageStorage)

  final def locationOf(image: ImageStorage): Option[SaveLocation] = mapping.getLeft(image)

  def move(image: ImageStorage, to: SaveLocation, info: SaveInfo)(implicit
      handler: ImageSaveCollisionHandler
  ): Boolean = {
    val newLocation = to
    val currentImage = get(newLocation)

    saveInfo(image) = info

    if (currentImage == null) {
      mapping.set(to, image)
      true
    } else if (currentImage != image) {
      handler.shouldReplaceImage(currentImage, image, newLocation) match {
        case Some(replace) =>
          if (replace) {
            mapping.removeRight(currentImage)
            set(newLocation, image)
            dispatcher.notify(ImagePool.Event.ImageReplaced(currentImage, image, newLocation))
          } else {
            mapping.removeRight(image)
            dispatcher.notify(ImagePool.Event.ImageReplaced(image, currentImage, newLocation))
          }
          true
        case None =>
          false
      }
    } else true
  }

  def save(image: ImageStorage, fileSystem: FileSystem): Boolean = {
    val success = (locationOf(image), saveInfo.get(image)) match {
      case (Some(loc), Some(info)) =>
        val oldImage = fileSystem.readImage(loc.file)
        val newImage =
          ImageUtils.overwritePartOfImage(image, info.format, loc.offset, oldImage)
        fileSystem.writeImage(newImage, loc.file)
      case _ =>
        false
    }

    if (success) dispatcher.notify(ImagePool.Event.ImageSaved(image))
    success
  }

  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage =
    ImageStorage.fromBGColor(bgColor, imageSize)

  // TODO: This pool system will not work since you can change SaveInfo for an image without telling the pool! Some planning has to be done.
  def fromFile(
      location: SaveLocation,
      format: StorageFormat,
      imageSize: Int,
      fileSystem: FileSystem
  ): Try[ImageStorage] = {
    if (contains(location)) Success(get(location))
    else {
      fileSystem.readImage(location.file) match {
        case Some(regularImage) =>
          val image =
            ImageStorage.fromRegularImage(regularImage, location.offset, format, imageSize)
          image.foreach(im => {
            set(location, im)
            saveInfo(im) = SaveInfo(format)
          })
          image
        case None =>
          Failure(new RuntimeException("no such image"))
      }
    }
  }
}
