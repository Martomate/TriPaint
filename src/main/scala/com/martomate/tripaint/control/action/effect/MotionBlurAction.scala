package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.effects.MotionBlurEffect
import com.martomate.tripaint.view.TriPaintView

class MotionBlurAction(model: TriPaintModel, askForMotionBlurRadius: () => Option[Int])
    extends EffectAction(model) {
  override protected def makeEffect(): Option[MotionBlurEffect] = {
    askForMotionBlurRadius().map(radius => new MotionBlurEffect(radius))
  }
}
