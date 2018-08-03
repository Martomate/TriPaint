package com.martomate.tripaint.image

import com.martomate.tripaint.Listenable
import com.martomate.tripaint.image.coords.TriangleCoords
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer

trait ImageGrid extends Listenable[ImageGridListener] {
  def imageSize: Int
  def images: Seq[TriImage]

  def apply(coords: TriImageCoords): Option[TriImage]
  def update(coords: TriImageCoords, image: TriImage): Unit
  def -=(coords: TriImageCoords): TriImage

  protected final def onAddImage(image: TriImage): Unit = notifyListeners(_.onAddImage(image))
  protected final def onRemoveImage(image: TriImage): Unit = notifyListeners(_.onRemoveImage(image))

  final def selectedImages: Seq[TriImage] = images.filter(_.editable)

  def selectImage(image: TriImage, replace: Boolean): Unit = {
    if (replace) images.foreach(im => im.content.editableProperty.value = im eq image)
    else image.content.editableProperty.value = !image.content.editableProperty.value
  }
}

class ImageGridSearcher(imageGrid: ImageGrid) {
  def search(startPos: PixelCoords, predicate: (PixelCoords, Color) => Boolean): Seq[PixelCoords] = {
    val visited = collection.mutable.Set.empty[PixelCoords]
    val result = collection.mutable.ArrayBuffer.empty[PixelCoords]
    val q = collection.mutable.Queue(startPos)
    visited += startPos

    while (q.nonEmpty) {
      val p = q.dequeue
      imageGrid(p.image) foreach { image =>
        val color = image.storage(p.pix)
        if (predicate(p, color)) {
          result += p

          val newOnes = p.neighbours(imageGrid.imageSize).filter(!visited(_))
          visited ++= newOnes
          q ++= newOnes
        }
      }
    }
    result
  }
}

class ImageGridImplOld(val imageSize: Int) extends ImageGrid {
  val images: ArrayBuffer[TriImage] = ArrayBuffer.empty[TriImage]

  override def apply(coords: TriImageCoords): Option[TriImage] = images.find(_.coords == coords)
  override def update(coords: TriImageCoords, image: TriImage): Unit = {
    val idx = images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val prev = images(idx)
      if (prev != image) onRemoveImage(prev)
      images(idx) = image
    }
    else images += image
    onAddImage(image)
  }
  override def -=(coords: TriImageCoords): TriImage = {
    val idx = images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val ret = images.remove(idx)
      onRemoveImage(ret)
      ret
    }
    else null
  }
}

trait ImageGridListener {
  def onAddImage(image: TriImage): Unit
  def onRemoveImage(image: TriImage): Unit
}

case class PixelCoords(pix: TriangleCoords, image: TriImageCoords) {
  def neighbours(imageSize: Int): Seq[PixelCoords] = {// TODO: reach into neighboring images
    val (x, y) = (pix.x, pix.y)
    val localCoords = Seq(
      TriangleCoords(x - 1, y),
      if (x % 2 == 0) TriangleCoords(x + 1, y + 1) else TriangleCoords(x - 1, y - 1),
      TriangleCoords(x + 1, y)
    ).filter(t => t.x >= 0 && t.y >= 0 && t.x < 2 * t.y + 1 && t.y < imageSize)
    localCoords.map(c => PixelCoords(c, image))
  }
}
