package com.martomate.tripaint.view.image.grid

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.util.Listenable

trait ImageGrid extends Listenable[ImageGridListener] {
  def imageSize: Int
  def images: Seq[ImageContent]

  def apply(coords: TriImageCoords): Option[ImageContent]
  def update(coords: TriImageCoords, image: ImageContent): Unit
  def -=(coords: TriImageCoords): ImageContent

  protected final def onAddImage(image: ImageContent): Unit = notifyListeners(_.onAddImage(image))
  protected final def onRemoveImage(image: ImageContent): Unit = notifyListeners(_.onRemoveImage(image))

  final def selectedImages: Seq[ImageContent] = images.filter(_.editable)
}
