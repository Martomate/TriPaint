package com.martomate.tripaint.control

import com.martomate.tripaint.control.action.*
import com.martomate.tripaint.control.action.effect.{
  BlurAction,
  MotionBlurAction,
  RandomNoiseAction,
  ScrambleAction
}
import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.gui.UIAction
import com.martomate.tripaint.view.{TriPaintView, TriPaintViewFactory, TriPaintViewListener}

class TriPaintController(val model: TriPaintModel, viewFactory: TriPaintViewFactory)
    extends TriPaintViewListener {
  val view: TriPaintView = viewFactory.createView(this, model)

  override def perform(action: UIAction): Unit = action match
    case UIAction.New =>
      perform(
        new NewAction(model, Color.fromFXColor(view.backgroundColor), view.askForWhereToPutImage)
      )
    case UIAction.Open =>
      perform(
        new OpenAction(
          model,
          view.askForFileToOpen,
          view.askForFileOpenSettings,
          view.askForWhereToPutImage
        )
      )
    case UIAction.OpenHexagon =>
      perform(
        new OpenHexagonAction(
          model,
          view.askForFileToOpen,
          view.askForFileOpenSettings,
          view.askForWhereToPutImage
        )
      )
    case UIAction.Save =>
      perform(
        new SaveAction(model, view.askForSaveFile, view.askForFileSaveSettings, view)
      )
    case UIAction.SaveAs =>
      perform(
        new SaveAsAction(model, view.askForSaveFile, view.askForFileSaveSettings, view)
      )
    case UIAction.Exit =>
      perform(
        new ExitAction(
          model,
          view.askForSaveFile,
          view.askForFileSaveSettings,
          view,
          view.askSaveBeforeClosing,
          view.close
        )
      )
    case UIAction.Undo => perform(new UndoAction(model))
    case UIAction.Redo => perform(new RedoAction(model))
    case UIAction.Blur => perform(new BlurAction(model, view.askForBlurRadius))
    case UIAction.MotionBlur =>
      perform(
        new MotionBlurAction(model, view.askForMotionBlurRadius)
      )
    case UIAction.RandomNoise =>
      perform(
        new RandomNoiseAction(model, view.askForRandomNoiseColors)
      )
    case UIAction.Scramble => perform(new ScrambleAction(model))
    case _                 =>

  private def perform(action: Action): Unit = action.perform()

  override def requestExit(): Boolean = new ExitAction(
    model,
    view.askForSaveFile,
    view.askForFileSaveSettings,
    view,
    view.askSaveBeforeClosing,
    view.close
  ).do_exit()
  override def requestImageRemoval(image: ImageContent): Unit = perform(
    new RemoveImageAction(
      image,
      model,
      view.askForSaveFile,
      view.askForFileSaveSettings,
      view,
      view.askSaveBeforeClosing
    )
  )
}
// dispatcher, store, view, action, ...
// model, view, intent
