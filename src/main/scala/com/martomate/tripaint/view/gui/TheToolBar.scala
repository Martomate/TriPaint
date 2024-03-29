package com.martomate.tripaint.view.gui

import com.martomate.tripaint.view.{MenuBarAction, TriPaintViewListener}

import scalafx.scene.control.{Button, Separator, ToolBar, Tooltip}
import scalafx.scene.image.ImageView

object TheToolBar {
  def create(controls: TriPaintViewListener): ToolBar = {
    val toolBar = new ToolBar
    toolBar.items = Seq(
      makeButton(controls, MainStageButtons.New),
      makeButton(controls, MainStageButtons.Open),
      makeButton(controls, MainStageButtons.Save),
      new Separator,
      makeButton(controls, MainStageButtons.Cut),
      makeButton(controls, MainStageButtons.Copy),
      makeButton(controls, MainStageButtons.Paste),
      new Separator,
      makeButton(controls, MainStageButtons.Undo),
      makeButton(controls, MainStageButtons.Redo)
    )
    toolBar
  }

  private def makeButton(controls: TriPaintViewListener, action: MenuBarAction): Button = {
    val item = if action.imagePath == null then {
      new Button(action.text)
    } else {
      new Button(null, new ImageView("icons/" + action.imagePath + ".png"))
    }
    item.onAction = _ => {
      if action.action != null then {
        controls.perform(action.action)
      }
    }
    item.tooltip = new Tooltip(action.text)
    item
  }
}
