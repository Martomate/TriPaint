package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.{RegularImage, SaveLocation, pool}
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.util.{InjectiveHashMap, InjectiveMap, Listenable}

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * This class should keep track of Map[SaveLocation, ImageStorage]
  * So if SaveAs then this class should be called so that collisions can be detected and dealt with,
  * like e.g. when you save A into the same place as B the user should be asked which image to keep,
  * and after that they will share the same ImageStorage.
  */
class ImagePool extends Listenable[ImagePoolListener] {
  private val mapping: InjectiveMap[SaveLocation, ImageStorage] = new InjectiveHashMap
  private val saveInfo: mutable.Map[ImageStorage, SaveInfo] = mutable.Map.empty

  private def contains(saveLocation: SaveLocation): Boolean = mapping.containsLeft(saveLocation)
  private def get(saveLocation: SaveLocation): ImageStorage = mapping.getRight(saveLocation).orNull
  private def set(saveLocation: SaveLocation, imageStorage: ImageStorage): Unit = mapping.set(saveLocation, imageStorage)

  final def locationOf(image: ImageStorage): Option[SaveLocation] = mapping.getLeft(image)
  final def saveInfoFor(image: ImageStorage): Option[SaveInfo] = saveInfo.get(image)

  def move(image: ImageStorage, to: SaveLocation, info: SaveInfo)(implicit handler: ImageSaveCollisionHandler): Boolean = {
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
            notifyListeners(_.onImageReplaced(currentImage, image, newLocation))
          } else {
            mapping.removeRight(image)
            notifyListeners(_.onImageReplaced(image, currentImage, newLocation))
          }
          true
        case None =>
          false
      }
    } else true
  }

  def save(image: ImageStorage, saver: ImageSaverToFile, fileSystem: FileSystem): Boolean = {
    val success = (locationOf(image), saveInfoFor(image)) match {
      case (Some(loc), Some(info)) =>
        val oldImage = fileSystem.readImage(loc.file).map(RegularImage.fromBufferedImage)
        val newImage = saver.overwritePartOfImage(image, info.format, loc.offset, oldImage)
        fileSystem.writeImage(newImage.toBufferedImage, loc.file)
      case _ =>
        false
    }

    if (success) notifyListeners(_.onImageSaved(image, saver))
    success
  }

  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage = ImageStorage.fromBGColor(bgColor, imageSize)

  // TODO: This pool system will not work since you can change SaveInfo for an image without telling the pool! Some planning has to be done.
  def fromFile(location: SaveLocation, format: StorageFormat, imageSize: Int, fileSystem: FileSystem): Try[ImageStorage] = {
    if (contains(location)) Success(get(location))
    else {
      fileSystem.readImage(location.file) match {
        case Some(storedImage) =>
          val regularImage = RegularImage.fromBufferedImage(storedImage)
          val image = ImageStorage.fromRegularImage(regularImage, location.offset, format, imageSize)
          image.foreach(im => {
              set(location, im)
              saveInfo(im) = pool.SaveInfo(format)
          })
          image
        case None =>
          Failure(new RuntimeException("no such image"))
      }
    }
  }
}

