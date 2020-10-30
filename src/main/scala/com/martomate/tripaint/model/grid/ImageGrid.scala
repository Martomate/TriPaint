package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.ImageChange
import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.effects.Effect
import com.martomate.tripaint.util.Listenable

import scala.collection.mutable

trait ImageGrid extends Listenable[ImageGridListener] {
  def imageSize: Int
  def setImageSizeIfEmpty(size: Int): Boolean

  def images: mutable.Seq[ImageContent]

  def apply(coords: TriImageCoords): Option[ImageContent]
  def update(coords: TriImageCoords, image: ImageContent): Unit
  def -=(coords: TriImageCoords): ImageContent

  protected final def onAddImage(image: ImageContent): Unit = notifyListeners(_.onAddImage(image))
  protected final def onRemoveImage(image: ImageContent): Unit = notifyListeners(_.onRemoveImage(image))

  final def selectedImages: mutable.Seq[ImageContent] = images.filter(_.editable)

  def applyEffect(effect: Effect): Unit = {
    val im = selectedImages

    val storages = im.map(_.storage)
    val allPixels = storages.map(_.allPixels)
    val before = allPixels.zip(storages).map(a => a._1.map(a._2(_)))

    effect.action(im.map(_.coords).toSeq, this)

    val after = allPixels.zip(storages).map(a => a._1.map(a._2(_)))

    for (here <- storages.indices) {
      val changed = for {
        neigh <- allPixels(here).indices
        if before(here)(neigh) != after(here)(neigh)
      } yield (allPixels(here)(neigh), before(here)(neigh), after(here)(neigh))

      if (changed.nonEmpty) {
        val change = new ImageChange(effect.name, im(here), changed)
        im(here).undoManager.append(change)
        im(here).changeTracker.tellListenersAboutBigChange()
      }
    }
  }
}
