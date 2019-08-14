package com.martomate.tripaint.control.action.effect

import com.martomate.tripaint.control.action.Action
import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.effects.Effect
import com.martomate.tripaint.view.TriPaintView

abstract class EffectAction extends Action {
  protected def makeEffect(view: TriPaintView): Option[Effect]

  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    makeEffect(view).foreach(applyEffect(model, _))
  }

  private def applyEffect(model: TriPaintModel, effect: Effect): Unit = {
    model.imageGrid.applyEffect(effect)
  }
}
