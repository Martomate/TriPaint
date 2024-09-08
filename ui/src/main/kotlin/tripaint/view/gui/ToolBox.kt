package tripaint.view.gui

import javafx.geometry.Orientation
import javafx.scene.control.ToggleButton
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane
import tripaint.util.MutableResource
import tripaint.view.EditMode

object ToolBox {
    fun create(
        editModes: List<EditMode>,
        currentEditMode: MutableResource<EditMode>,
    ): TilePane {
        val buttons: MutableList<ToolboxButton> = mutableListOf()
        val buttonMap: MutableMap<EditMode, ToolboxButton> = mutableMapOf()

        editModes.map { m ->
            val b = ToolboxButton(m) { currentEditMode.value = m }
            if (m == currentEditMode.value) {
                b.isSelected = true
            }
            buttons += b
            buttonMap[m] = b
        }

        currentEditMode.onChange { (oldMode, newMode) ->
            buttonMap[oldMode]?.isSelected = false
            buttonMap[newMode]?.isSelected = true
        }

        return TilePane(Orientation.VERTICAL, *buttons.toTypedArray())
    }
}

class ToolboxButton(mode: EditMode, onClick: () -> Unit) : ToggleButton(null, ImageView("icons/editmodes/" + mode.imagePath + ".png")) {
    init {
        tooltip = Tooltip("${mode.tooltipText}\n(Shortcut: ${mode.shortCut})")
        setOnAction { _ -> onClick() }

        (graphic as ImageView).fitWidth = 20.0
        (graphic as ImageView).fitHeight = 20.0
    }
}
