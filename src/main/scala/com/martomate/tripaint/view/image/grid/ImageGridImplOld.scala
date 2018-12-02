package com.martomate.tripaint.view.image.grid

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.view.image.TriImage

import scala.collection.mutable.ArrayBuffer

class ImageGridImplOld(val imageSize: Int) extends ImageGrid {
  val images: ArrayBuffer[TriImage] = ArrayBuffer.empty[TriImage]

  override def apply(coords: TriImageCoords): Option[TriImage] = images.find(_.content.coords == coords)
  override def update(coords: TriImageCoords, image: TriImage): Unit = {
    val idx = images.indexWhere(_.content.coords == coords)
    if (idx != -1) {
      val prev = images(idx)
      if (prev != image) onRemoveImage(prev)
      images(idx) = image
    }
    else images += image
    onAddImage(image)
  }
  override def -=(coords: TriImageCoords): TriImage = {
    val idx = images.indexWhere(_.content.coords == coords)
    if (idx != -1) {
      val ret = images.remove(idx)
      onRemoveImage(ret)
      ret
    }
    else null
  }
}
