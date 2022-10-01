package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.effects.RandomNoiseEffect
import com.martomate.tripaint.view.TriPaintView
import scalafx.scene.paint.Color

class RandomNoiseAction(model: TriPaintModel, askForRandomNoiseColors: () => Option[(Color, Color)])
    extends EffectAction(model) {
  override protected def makeEffect(): Option[RandomNoiseEffect] = {
    askForRandomNoiseColors() map { case (lo, hi) =>
      new RandomNoiseEffect(lo, hi)
    }
  }
}
