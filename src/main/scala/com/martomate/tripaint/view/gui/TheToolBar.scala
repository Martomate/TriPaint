package com.martomate.tripaint.view.gui

import scalafx.scene.control.{Separator, ToolBar}

object TheToolBar:
  def create(controls: MainStageButtons): ToolBar =
    val toolBar = new ToolBar
    toolBar.items = Seq(
      controls.New.button,
      controls.Open.button,
      controls.Save.button,
      new Separator,
      controls.Cut.button,
      controls.Copy.button,
      controls.Paste.button,
      new Separator,
      controls.Undo.button,
      controls.Redo.button
    )
    toolBar
