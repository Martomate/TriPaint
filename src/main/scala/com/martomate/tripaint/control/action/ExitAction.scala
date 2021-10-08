package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.view.TriPaintView

object ExitAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    if (do_exit(model, view)) view.close()
  }

  def do_exit(model: TriPaintModel, view: TriPaintView): Boolean = {
    allImages(model).filter(_.changed) match {
      case Seq() => true
      case images =>
        saveBeforeClosing(view, images: _*) match {
          case Some(shouldSave) =>
            if (shouldSave) save(model, view, images: _*)
            else true
          case None => false
        }
    }
  }
}
