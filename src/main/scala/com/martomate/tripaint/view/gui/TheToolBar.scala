package com.martomate.tripaint.view.gui

import com.martomate.tripaint.control.TriPaintController
import scalafx.scene.control.{Separator, ToolBar}

class TheToolBar(controls: TriPaintController) extends ToolBar {
  items = Seq(
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
}
