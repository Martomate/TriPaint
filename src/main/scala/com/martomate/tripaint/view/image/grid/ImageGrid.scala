package com.martomate.tripaint.view.image.grid

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.util.Listenable
import com.martomate.tripaint.view.image.TriImage

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
