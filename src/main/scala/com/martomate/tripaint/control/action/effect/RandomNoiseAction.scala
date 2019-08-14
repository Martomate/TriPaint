package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.model.effects.RandomNoiseEffect
import com.martomate.tripaint.view.TriPaintView

object RandomNoiseAction extends EffectAction {
  override protected def makeEffect(view: TriPaintView): Option[RandomNoiseEffect] = {
    view.askForRandomNoiseColors() map {
      case (lo, hi) => new RandomNoiseEffect(lo, hi)
    }
  }
}
