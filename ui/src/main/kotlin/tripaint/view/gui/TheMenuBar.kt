package tripaint.view.gui

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import tripaint.view.MenuBarAction
import tripaint.view.TriPaintViewListener

object TheMenuBar {
    fun create(controls: TriPaintViewListener): MenuBar {
        val fileMenu = makeMenu(
            "File",
            makeMenuItem(controls, MainStageButtons.New),
            makeMenuItem(controls, MainStageButtons.Open),
            makeMenuItem(controls, MainStageButtons.OpenHexagon),
            SeparatorMenuItem(),
            makeMenuItem(controls, MainStageButtons.Save),
            makeMenuItem(controls, MainStageButtons.SaveAs),
            SeparatorMenuItem(),
            makeMenuItem(controls, MainStageButtons.Exit)
        )

        val editMenu = makeMenu(
            "Edit",
            makeMenuItem(controls, MainStageButtons.Undo),
            makeMenuItem(controls, MainStageButtons.Redo),
            SeparatorMenuItem(),
            makeMenuItem(controls, MainStageButtons.Cut),
            makeMenuItem(controls, MainStageButtons.Copy),
            makeMenuItem(controls, MainStageButtons.Paste)
        )

        val viewMenu = makeMenu(
            "View",
            makeMenuItem(controls, MainStageButtons.ShowPreview),
        )

        val organizeMenu =
            makeMenu(
                "Organize",
                makeMenuItem(controls, MainStageButtons.Move),
                makeMenuItem(controls, MainStageButtons.Scale),
                makeMenuItem(controls, MainStageButtons.Rotate)
            )

        val effectsMenu = makeMenu(
            "Effects",
            makeMenuItem(controls, MainStageButtons.Blur),
            makeMenuItem(controls, MainStageButtons.MotionBlur),
            makeMenuItem(controls, MainStageButtons.RandomNoise),
            makeMenuItem(controls, MainStageButtons.Scramble),
            makeMenuItem(controls, MainStageButtons.Cell)
        )

        val menuBar = MenuBar()
        menuBar.isUseSystemMenuBar = true
        menuBar.menus.setAll(fileMenu, editMenu, viewMenu, organizeMenu, effectsMenu)
        return menuBar
    }

    private fun makeMenu(text: String, vararg menuItems: MenuItem): Menu {
        return Menu(text, null, *menuItems)
    }

    private fun makeMenuItem(controls: TriPaintViewListener, action: MenuBarAction): MenuItem {
        val item = MenuItem(action.text)
        if (action.action != null) {
            item.setOnAction { _ -> controls.perform(action.action) }
        }
        if (action.accelerator != null) {
            item.accelerator = action.accelerator
        }
        return item
    }
}