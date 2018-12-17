package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.ImageChange
import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.effects.Effect
import com.martomate.tripaint.util.Listenable

trait ImageGrid extends Listenable[ImageGridListener] {
  def imageSize: Int
  def setImageSizeIfEmpty(size: Int): Boolean

  def images: Seq[ImageContent]

  def apply(coords: TriImageCoords): Option[ImageContent]
  def update(coords: TriImageCoords, image: ImageContent): Unit
  def -=(coords: TriImageCoords): ImageContent

  protected final def onAddImage(image: ImageContent): Unit = notifyListeners(_.onAddImage(image))
  protected final def onRemoveImage(image: ImageContent): Unit = notifyListeners(_.onRemoveImage(image))

  final def selectedImages: Seq[ImageContent] = images.filter(_.editable)

  def applyEffect(effect: Effect): Unit = {
    val im = selectedImages

    val storages = im.map(a => a.storage)
    val allPixels = storages.map(_.allPixels)
    val start = allPixels.zip(storages).map(a => a._1.map(a._2(_)))

    effect.action(im.map(_.coords), this)

    val end = allPixels.zip(storages).map(a => a._1.map(a._2(_)))

    val changed = allPixels.indices.map(s => allPixels(s).indices.map(i => (allPixels(s)(i), start(s)(i), end(s)(i))).filter(p => p._2 != p._3))
    for (i <- storages.indices) {
      val change = new ImageChange(effect.name, im(i), changed(i))
      im(i).undoManager.append(change)
      im(i).changeTracker.tellListenersAboutBigChange()
    }
  }
}
