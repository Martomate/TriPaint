package com.martomate.tripaint.view.image.grid

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriImageCoords

import scala.collection.mutable.ArrayBuffer

class ImageGridImplOld(val imageSize: Int) extends ImageGrid {
  val images: ArrayBuffer[ImageContent] = ArrayBuffer.empty

  override def apply(coords: TriImageCoords): Option[ImageContent] = images.find(_.coords == coords)
  override def update(coords: TriImageCoords, image: ImageContent): Unit = {
    val idx = images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val prev = images(idx)
      if (prev != image) onRemoveImage(prev)
      images(idx) = image
    }
    else images += image
    onAddImage(image)
  }
  override def -=(coords: TriImageCoords): ImageContent = {
    val idx = images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val ret = images.remove(idx)
      onRemoveImage(ret)
      ret
    }
    else null
  }
}
