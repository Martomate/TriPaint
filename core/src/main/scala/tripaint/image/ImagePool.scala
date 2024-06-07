package tripaint.image

import tripaint.coords.StorageCoords
import tripaint.image.format.StorageFormat
import tripaint.util.{InjectiveHashMap, InjectiveMap}

import java.io.File
import scala.collection.mutable

object ImagePool {
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

  final def locationOf(image: ImageStorage): Option[SaveLocation] = mapping.getLeft(image)

  def getSaveLocationAndInfo(image: ImageStorage): (Option[SaveLocation], Option[SaveInfo]) =
    (mapping.getLeft(image), saveInfo.get(image))

  def imageAt(location: SaveLocation): Option[ImageStorage] = mapping.getRight(location)

  def set(image: ImageStorage, location: SaveLocation, info: SaveInfo): Unit =
    mapping.getRight(location).foreach(this.remove)
    mapping.set(location, image)
    saveInfo(image) = info

  def remove(image: ImageStorage): Unit =
    mapping.removeRight(image)
    saveInfo.remove(image)
}
