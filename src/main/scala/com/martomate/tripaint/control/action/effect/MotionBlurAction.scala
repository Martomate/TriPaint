package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.model.effects.MotionBlurEffect
import com.martomate.tripaint.view.TriPaintView

object MotionBlurAction extends EffectAction {
  override protected def makeEffect(view: TriPaintView): Option[MotionBlurEffect] = {
    view.askForMotionBlurRadius().map(radius => new MotionBlurEffect(radius))
  }
}
