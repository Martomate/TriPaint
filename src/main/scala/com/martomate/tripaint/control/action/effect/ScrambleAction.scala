package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.model.effects.{Effect, ScrambleEffect}
import com.martomate.tripaint.view.TriPaintView

object ScrambleAction extends EffectAction {
  override protected def makeEffect(view: TriPaintView): Option[Effect] = Some(ScrambleEffect)
}
