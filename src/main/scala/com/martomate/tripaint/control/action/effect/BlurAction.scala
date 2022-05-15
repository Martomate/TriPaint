package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.effects.BlurEffect
import com.martomate.tripaint.view.TriPaintView

class BlurAction(model: TriPaintModel, askForBlurRadius: () => Option[Int]) extends EffectAction(model) {
  override protected def makeEffect(): Option[BlurEffect] = {
    askForBlurRadius().map(new BlurEffect(_))
  }
}
