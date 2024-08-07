package tripaint.view.gui

import tripaint.view.{MenuBarAction, TriPaintViewListener}

import javafx.scene.control.{Menu, MenuBar, MenuItem, SeparatorMenuItem}
import javafx.scene.image.ImageView

object TheMenuBar {
  def create(controls: TriPaintViewListener): MenuBar = {
    val menu_file = makeMenu(
      "File",
      makeMenuItem(controls, MainStageButtons.New),
      makeMenuItem(controls, MainStageButtons.Open),
      makeMenuItem(controls, MainStageButtons.OpenHexagon),
      new SeparatorMenuItem,
      makeMenuItem(controls, MainStageButtons.Save),
      makeMenuItem(controls, MainStageButtons.SaveAs),
      new SeparatorMenuItem,
      makeMenuItem(controls, MainStageButtons.Exit)
    )

    val menu_edit = makeMenu(
      "Edit",
      makeMenuItem(controls, MainStageButtons.Undo),
      makeMenuItem(controls, MainStageButtons.Redo),
      new SeparatorMenuItem,
      makeMenuItem(controls, MainStageButtons.Cut),
      makeMenuItem(controls, MainStageButtons.Copy),
      makeMenuItem(controls, MainStageButtons.Paste)
    )

    val menu_organize =
      makeMenu(
        "Organize",
        makeMenuItem(controls, MainStageButtons.Move),
        makeMenuItem(controls, MainStageButtons.Scale),
        makeMenuItem(controls, MainStageButtons.Rotate)
      )

    val menu_effects = makeMenu(
      "Effects",
      makeMenuItem(controls, MainStageButtons.Blur),
      makeMenuItem(controls, MainStageButtons.MotionBlur),
      makeMenuItem(controls, MainStageButtons.RandomNoise),
      makeMenuItem(controls, MainStageButtons.Scramble),
      makeMenuItem(controls, MainStageButtons.Cell)
    )

    val menuBar = new MenuBar
    menuBar.setUseSystemMenuBar(true)
    menuBar.getMenus.setAll(menu_file, menu_edit, menu_organize, menu_effects)
    menuBar
  }

  private def makeMenu(text: String, menuItems: MenuItem*): Menu = {
    new Menu(text, null, menuItems*)
  }

  private def makeMenuItem(controls: TriPaintViewListener, action: MenuBarAction): MenuItem = {
    val item = if action.imagePath == null then {
      new MenuItem(action.text)
    } else {
      new MenuItem(action.text, new ImageView(s"icons/${action.imagePath}.png"))
    }
    if action.action != null then {
      item.setOnAction(_ => controls.perform(action.action))
    }
    if action.accelerator != null then {
      item.setAccelerator(action.accelerator)
    }
    item
  }
}
