package com.martomate.tripaint.model.pool

import com.martomate.tripaint.model.{SaveInfo, SaveLocation}
import com.martomate.tripaint.model.format.StorageFormat
import com.martomate.tripaint.model.save.ImageSaver
import com.martomate.tripaint.model.storage.{ImageStorage, ImageStorageFactory}
import com.martomate.tripaint.util.{InjectiveMap, Listenable}
import scalafx.scene.paint.Color

import scala.collection.mutable
import scala.util.{Success, Try}

/**
  * This class should keep track of Map[SaveLocation, ImageStorage]
  * So if SaveAs then this class should be called so that collisions can be detected and dealt with,
  * like e.g. when you save A into the same place as B the user should be asked which image to keep,
  * and after that they will share the same ImageStorage.
  */
abstract class ImagePool(factory: ImageStorageFactory) extends ImageStorageFactory with Listenable[ImagePoolListener] {

  protected val mapping: InjectiveMap[SaveLocation, ImageStorage]
  protected val saveInfo: mutable.Map[ImageStorage, SaveInfo]

  protected final def contains(saveLocation: SaveLocation): Boolean = mapping.containsLeft(saveLocation)
  protected final def get(saveLocation: SaveLocation): ImageStorage = mapping.getRight(saveLocation).orNull
  protected final def set(saveLocation: SaveLocation, imageStorage: ImageStorage): Unit = mapping.set(saveLocation, imageStorage)

  final def locationOf(image: ImageStorage): Option[SaveLocation] = mapping.getLeft(image)
  final def saveInfoFor(image: ImageStorage): Option[SaveInfo] = saveInfo.get(image)

  def move(image: ImageStorage, to: SaveLocation, info: SaveInfo)(implicit collisionHandler: ImageSaveCollisionHandler): Boolean

  def save(image: ImageStorage, saver: ImageSaver): Boolean = {
    val success =
      locationOf(image)
        .flatMap(loc => saveInfoFor(image)
          .map(info => saver.save(image, info.format, loc)))
        .getOrElse(false)

    if (success) notifyListeners(_.onImageSaved(image, saver))
    success
  }

  override def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage = factory.fromBGColor(bgColor, imageSize)

  // TODO: This pool system will not work since you can change SaveInfo for an image without telling the pool! Some planning has to be done.
  override def fromFile(location: SaveLocation, format: StorageFormat, imageSize: Int): Try[ImageStorage] = {
    if (contains(location)) Success(get(location))
    else {
      val image = factory.fromFile(location, format, imageSize)
      image.foreach(im => {
        set(location, im)
        saveInfo(im) = SaveInfo(format)
      })
      image
    }
  }
}

