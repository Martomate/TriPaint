package com.martomate.tripaint.control

import com.martomate.tripaint.control.action.effect.{BlurAction, MotionBlurAction, RandomNoiseAction, ScrambleAction}
import com.martomate.tripaint.control.action._
import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.view.gui.MainStage
import com.martomate.tripaint.view.{TriPaintView, TriPaintViewListener}

class TriPaintController(val model: TriPaintModel) extends TriPaintViewListener {
  val view: TriPaintView = new MainStage(this, model)

  model.imageGrid.setImageSizeIfEmpty(view.askForImageSize().getOrElse(32))

  override def action_new(): Unit = NewAction.perform(model, view)
  override def action_open(): Unit = OpenAction.perform(model, view)
  override def action_openHexagon(): Unit = OpenHexagonAction.perform(model, view)
  override def action_save(): Unit = SaveAction.perform(model, view)
  override def action_saveAs(): Unit = SaveAsAction.perform(model, view)
  override def action_exit(): Unit = ExitAction.perform(model, view)

  override def action_undo(): Unit = UndoAction.perform(model, view)
  override def action_redo(): Unit = RedoAction.perform(model, view)

  override def action_blur(): Unit = BlurAction.perform(model, view)
  override def action_motionBlur(): Unit = MotionBlurAction.perform(model, view)
  override def action_randomNoise(): Unit = RandomNoiseAction.perform(model, view)
  override def action_scramble(): Unit = ScrambleAction.perform(model, view)

  override def requestExit(): Boolean = ExitAction.do_exit(model, view)
  override def requestImageRemoval(image: ImageContent): Unit = new RemoveImageAction(image).perform(model, view)
}
