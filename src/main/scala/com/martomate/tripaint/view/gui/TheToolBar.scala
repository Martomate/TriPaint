package com.martomate.tripaint.view.gui

import com.martomate.tripaint.view.MenuBarAction
import scalafx.scene.control.{Button, Separator, ToolBar, Tooltip}
import scalafx.scene.image.ImageView

object TheToolBar:
  def create(controls: MainStageButtons): ToolBar =
    val toolBar = new ToolBar
    toolBar.items = Seq(
      makeButton(controls.New),
      makeButton(controls.Open),
      makeButton(controls.Save),
      new Separator,
      makeButton(controls.Cut),
      makeButton(controls.Copy),
      makeButton(controls.Paste),
      new Separator,
      makeButton(controls.Undo),
      makeButton(controls.Redo)
    )
    toolBar

  private def makeButton(action: MenuBarAction): Button =
    val item =
      if action.imagePath == null
      then new Button(action.text)
      else new Button(null, new ImageView("icons/" + action.imagePath + ".png"))
    item.onAction = _ => action.onAction()
    item.tooltip = new Tooltip(action.text)
    item
