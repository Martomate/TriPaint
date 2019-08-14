package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.model.effects.BlurEffect
import com.martomate.tripaint.view.TriPaintView

object BlurAction extends EffectAction {
  override protected def makeEffect(view: TriPaintView): Option[BlurEffect] = {
    view.askForBlurRadius().map(new BlurEffect(_))
  }
}
