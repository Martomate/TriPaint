package com.martomate.tripaint.model.image

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.StorageFormat
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
  import ImagePool.{SaveInfo, SaveLocation}

  private val mapping: InjectiveMap[SaveLocation, ImageStorage] = new InjectiveHashMap
  private val saveInfo: mutable.Map[ImageStorage, SaveInfo] = mutable.Map.empty

  private val dispatcher = new EventDispatcher[ImagePool.Event]
  def trackChanges(tracker: Tracker[ImagePool.Event]): Unit = dispatcher.track(tracker)

  final def locationOf(image: ImageStorage): Option[SaveLocation] = mapping.getLeft(image)

  def getSaveLocationAndInfo(image: ImageStorage): (Option[SaveLocation], Option[SaveInfo]) =
    (locationOf(image), saveInfo.get(image))

  def move(image: ImageStorage, to: SaveLocation, info: SaveInfo)(using
      handler: ImageSaveCollisionHandler
  ): Boolean =
    val newLocation = to
    val currentImage = mapping.getRight(newLocation).orNull

    saveInfo(image) = info

    if currentImage == null then
      mapping.set(to, image)
      true
    else if currentImage != image then
      val choice = handler.shouldReplaceImage(currentImage, image, newLocation)
      choice match
        case Some(true) =>
          mapping.removeRight(currentImage)
          mapping.set(newLocation, image)
          dispatcher.notify(ImagePool.Event.ImageReplaced(currentImage, image, newLocation))
        case Some(false) =>
          mapping.removeRight(image)
          dispatcher.notify(ImagePool.Event.ImageReplaced(image, currentImage, newLocation))
        case None =>
      choice.isDefined
    else true

  // TODO: This pool system will not work since you can change SaveInfo for an image without telling the pool! Some planning has to be done.
  def fromFile(
      location: SaveLocation,
      format: StorageFormat,
      imageSize: Int,
      fileSystem: FileSystem
  ): Try[ImageStorage] =
    if mapping.containsLeft(location)
    then Success(mapping.getRight(location).orNull)
    else
      fileSystem.readImage(location.file) match
        case Some(regularImage) =>
          val image =
            ImageStorage.fromRegularImage(regularImage, location.offset, format, imageSize)

          image match
            case Success(im) =>
              mapping.set(location, im)
              saveInfo(im) = SaveInfo(format)
            case _ =>

          image
        case None =>
          Failure(new RuntimeException("no such image"))

}
