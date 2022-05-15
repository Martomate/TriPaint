package com.martomate.tripaint.control

import com.martomate.tripaint.control.action.*
import com.martomate.tripaint.control.action.effect.{BlurAction, MotionBlurAction, RandomNoiseAction, ScrambleAction}
import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.{TriPaintView, TriPaintViewFactory, TriPaintViewListener}

class TriPaintController(val model: TriPaintModel, viewFactory: TriPaintViewFactory) extends TriPaintViewListener {
  val view: TriPaintView = viewFactory.createView(this, model)

  private def perform(action: Action): Unit = action.perform()

  override def action_new(): Unit = perform(new NewAction(model, Color.fromFXColor(view.backgroundColor), view.askForWhereToPutImage))
  override def action_open(): Unit = perform(new OpenAction(model, view.askForFileToOpen, view.askForFileOpenSettings, view.askForWhereToPutImage))
  override def action_openHexagon(): Unit = perform(new OpenHexagonAction(model, view.askForFileToOpen, view.askForFileOpenSettings, view.askForWhereToPutImage))
  override def action_save(): Unit = perform(new SaveAction(model, view.askForSaveFile, view.askForFileSaveSettings, view))
  override def action_saveAs(): Unit = perform(new SaveAsAction(model, view.askForSaveFile, view.askForFileSaveSettings, view))
  override def action_exit(): Unit = perform(new ExitAction(model, view.askForSaveFile, view.askForFileSaveSettings, view, view.askSaveBeforeClosing, view.close))

  override def action_undo(): Unit = perform(new UndoAction(model))
  override def action_redo(): Unit = perform(new RedoAction(model))

  override def action_blur(): Unit = perform(new BlurAction(model, view.askForBlurRadius))
  override def action_motionBlur(): Unit = perform(new MotionBlurAction(model, view.askForMotionBlurRadius))
  override def action_randomNoise(): Unit = perform(new RandomNoiseAction(model, view.askForRandomNoiseColors))
  override def action_scramble(): Unit = perform(new ScrambleAction(model))

  override def requestExit(): Boolean = new ExitAction(model, view.askForSaveFile, view.askForFileSaveSettings, view, view.askSaveBeforeClosing, view.close).do_exit()
  override def requestImageRemoval(image: ImageContent): Unit = perform(new RemoveImageAction(image, model, view.askForSaveFile, view.askForFileSaveSettings, view, view.askSaveBeforeClosing))
}
// dispatcher, store, view, action, ...
// model, view, intent
