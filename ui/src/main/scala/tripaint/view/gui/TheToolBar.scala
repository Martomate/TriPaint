package tripaint.view.gui

import tripaint.view.{MenuBarAction, TriPaintViewListener}

import javafx.scene.control.{Button, Separator, ToolBar, Tooltip}
import javafx.scene.image.ImageView

object TheToolBar {
  def create(controls: TriPaintViewListener): ToolBar = {
    val toolBar = new ToolBar
    toolBar.getItems.setAll(
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
    item.setOnAction(_ => {
      if action.action != null then {
        controls.perform(action.action)
      }
    })
    item.setTooltip(new Tooltip(action.text))
    item
  }
}
