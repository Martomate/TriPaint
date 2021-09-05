package com.martomate.tripaint.control

import com.martomate.tripaint.control.action._
import com.martomate.tripaint.control.action.effect.{BlurAction, MotionBlurAction, RandomNoiseAction, ScrambleAction}
import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.{TriPaintView, TriPaintViewFactory, TriPaintViewListener}

class TriPaintController(val model: TriPaintModel, viewFactory: TriPaintViewFactory) extends TriPaintViewListener {
  val view: TriPaintView = viewFactory.createView(this, model)

  private def perform(action: Action): Unit = action.perform(model, view)

  override def action_new(): Unit = perform(NewAction)
  override def action_open(): Unit = perform(OpenAction)
  override def action_openHexagon(): Unit = perform(OpenHexagonAction)
  override def action_save(): Unit = perform(SaveAction)
  override def action_saveAs(): Unit = perform(SaveAsAction)
  override def action_exit(): Unit = perform(ExitAction)

  override def action_undo(): Unit = perform(UndoAction)
  override def action_redo(): Unit = perform(RedoAction)

  override def action_blur(): Unit = perform(BlurAction)
  override def action_motionBlur(): Unit = perform(MotionBlurAction)
  override def action_randomNoise(): Unit = perform(RandomNoiseAction)
  override def action_scramble(): Unit = perform(ScrambleAction)

  override def requestExit(): Boolean = ExitAction.do_exit(model, view)
  override def requestImageRemoval(image: ImageContent): Unit = perform(new RemoveImageAction(image))
}
