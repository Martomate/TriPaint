package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.effects.Effect
import com.martomate.tripaint.model.image.content.{ImageChange, PixelChange}
import com.martomate.tripaint.view.TriPaintView

class EffectAction(model: TriPaintModel, effect: Effect) {
  def perform(): Unit = {
    applyEffect(model, effect)
  }

  private def applyEffect(model: TriPaintModel, effect: Effect): Unit = {
    val grid = model.imageGrid
    val im = grid.selectedImages

    val storages = im.map(_.storage)
    val allPixels = storages.map(_.allPixels)
    val before = allPixels.zip(storages).map(a => a._1.map(a._2(_)))

    effect.action(im.map(_.coords), grid)

    val after = allPixels.zip(storages).map(a => a._1.map(a._2(_)))

    for (here <- storages.indices) {
      val changed = for {
        neigh <- allPixels(here).indices
        if before(here)(neigh) != after(here)(neigh)
      } yield PixelChange(allPixels(here)(neigh), before(here)(neigh), after(here)(neigh))

      if (changed.nonEmpty) {
        val change = new ImageChange(effect.name, im(here), changed)
        im(here).undoManager.append(change)
        im(here).tellListenersAboutBigChange()
      }
    }
  }
}
