package com.martomate.tripaint.image.storage

import com.martomate.tripaint.image.SaveLocation
import com.martomate.tripaint.image.save.ImageSaver
import com.martomate.tripaint.{InjectiveMap, Listenable}
import scalafx.scene.paint.Color

import scala.util.{Success, Try}

/**
  * This class should keep track of Map[SaveLocation, ImageStorage]
  * So if SaveAs then this class should be called so that collisions can be detected and dealt with,
  * like e.g. when you save A into the same place as B the user should be asked which image to keep,
  * and after that they will share the same ImageStorage.
  */
abstract class ImagePool(factory: ImageStorageFactory, collisionHandler: ImageSaveCollisionHandler) extends ImageStorageFactory with Listenable[ImagePoolListener] {

  protected val mapping: InjectiveMap[SaveLocation, ImageStorage]

  protected final def contains(saveLocation: SaveLocation): Boolean = mapping.containsLeft(saveLocation)
  protected final def get(saveLocation: SaveLocation): ImageStorage = mapping.getRight(saveLocation).orNull
  protected final def set(saveLocation: SaveLocation, imageStorage: ImageStorage): Unit = mapping.set(saveLocation, imageStorage)

  final def locationOf(image: ImageStorage): Option[SaveLocation] = mapping.getLeft(image)

  def move(image: ImageStorage, to: SaveLocation): Boolean

  def save(image: ImageStorage, saver: ImageSaver): Boolean = {
    val success = mapping.getLeft(image).exists(loc => saver.save(image, loc))
    if (success) notifyListeners(_.onImageSaved(image, saver))
    success
  }

  override def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage = factory.fromBGColor(bgColor, imageSize)

  // TODO: This pool system will not work since you can change SaveInfo for an image without telling the pool! Some planning has to be done.
  override def fromFile(saveInfo: SaveLocation, imageSize: Int): Try[ImageStorage] = {
    if (contains(saveInfo)) Success(get(saveInfo))
    else {
      val image = factory.fromFile(saveInfo, imageSize)
      image.foreach(im => set(saveInfo, im))
      image
    }
  }
}

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(currentImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Option[Boolean]
}

trait ImagePoolListener {
  def onImageSaved(image: ImageStorage, saver: ImageSaver): Unit
  def onImageReplaced(oldImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Unit
}
