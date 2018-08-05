package com.martomate.tripaint.image.grid

import com.martomate.tripaint.Listenable
import com.martomate.tripaint.image.coords.TriImageCoords
import com.martomate.tripaint.image.graphics.TriImage

trait ImageGrid extends Listenable[ImageGridListener] {
  def imageSize: Int
  def images: Seq[TriImage]

  def apply(coords: TriImageCoords): Option[TriImage]
  def update(coords: TriImageCoords, image: TriImage): Unit
  def -=(coords: TriImageCoords): TriImage

  protected final def onAddImage(image: TriImage): Unit = notifyListeners(_.onAddImage(image))
  protected final def onRemoveImage(image: TriImage): Unit = notifyListeners(_.onRemoveImage(image))

  final def selectedImages: Seq[TriImage] = images.filter(_.content.editable)
}
