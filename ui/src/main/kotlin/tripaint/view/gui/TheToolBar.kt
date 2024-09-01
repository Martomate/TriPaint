package tripaint.view.gui

import javafx.scene.control.Button
import javafx.scene.control.Separator
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import tripaint.view.TriPaintViewListener
import javafx.scene.image.ImageView
import tripaint.view.MenuBarAction

object TheToolBar {
    fun create(controls: TriPaintViewListener): ToolBar {
        val toolBar = ToolBar()
        toolBar.items.setAll(
            makeButton(controls, MainStageButtons.New),
            makeButton(controls, MainStageButtons.Open),
            makeButton(controls, MainStageButtons.Save),
            Separator(),
            makeButton(controls, MainStageButtons.Cut),
            makeButton(controls, MainStageButtons.Copy),
            makeButton(controls, MainStageButtons.Paste),
            Separator(),
            makeButton(controls, MainStageButtons.Undo),
            makeButton(controls, MainStageButtons.Redo)
        )
        return toolBar
    }

    private fun makeButton(controls: TriPaintViewListener, action: MenuBarAction): Button {
        val item = if (action.imagePath == null) {
            Button(action.text)
        } else {
            val imageView = ImageView("icons/${action.imagePath}.png")
            imageView.fitWidth = 20.0
            imageView.fitHeight = 20.0
            Button(null, imageView)
        }
        item.setOnAction { _ ->
            if (action.action != null) {
                controls.perform(action.action)
            }
        }
        item.tooltip = Tooltip(action.text)
        return item
    }
}