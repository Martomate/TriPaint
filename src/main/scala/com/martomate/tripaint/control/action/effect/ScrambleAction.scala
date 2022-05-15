package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.effects.{Effect, ScrambleEffect}
import com.martomate.tripaint.view.TriPaintView

class ScrambleAction(model: TriPaintModel) extends EffectAction(model) {
  override protected def makeEffect(): Option[Effect] = Some(ScrambleEffect)
}
