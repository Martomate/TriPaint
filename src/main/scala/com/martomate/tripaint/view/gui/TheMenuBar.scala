package com.martomate.tripaint.view.gui

import com.martomate.tripaint.view.MenuBarAction
import scalafx.scene.control.{Menu, MenuBar, MenuItem, SeparatorMenuItem}
import scalafx.scene.image.ImageView

object TheMenuBar:
  def create(controls: MainStageButtons): MenuBar =
    val menu_file = makeMenu(
      "File",
      makeMenuItem(controls.New),
      makeMenuItem(controls.Open),
      makeMenuItem(controls.OpenHexagon),
      new SeparatorMenuItem,
      makeMenuItem(controls.Save),
      makeMenuItem(controls.SaveAs),
      new SeparatorMenuItem,
      makeMenuItem(controls.Exit)
    )

    val menu_edit = makeMenu(
      "Edit",
      makeMenuItem(controls.Undo),
      makeMenuItem(controls.Redo),
      new SeparatorMenuItem,
      makeMenuItem(controls.Cut),
      makeMenuItem(controls.Copy),
      makeMenuItem(controls.Paste)
    )

    val menu_organize =
      makeMenu(
        "Organize",
        makeMenuItem(controls.Move),
        makeMenuItem(controls.Scale),
        makeMenuItem(controls.Rotate)
      )

    val menu_effects = makeMenu(
      "Effects",
      makeMenuItem(controls.Blur),
      makeMenuItem(controls.MotionBlur),
      makeMenuItem(controls.RandomNoise),
      makeMenuItem(controls.Scramble)
    )

    val menuBar = new MenuBar
    menuBar.useSystemMenuBar = true
    menuBar.menus = Seq(menu_file, menu_edit, menu_organize, menu_effects)
    menuBar

  private def makeMenu(text: String, menuItems: MenuItem*): Menu =
    val menu = new Menu(text)
    menu.items = menuItems
    menu

  private def makeMenuItem(action: MenuBarAction): MenuItem =
    val item =
      if action.imagePath == null
      then new MenuItem(action.text)
      else new MenuItem(action.text, new ImageView("icons/" + action.imagePath + ".png"))
    item.onAction = _ => action.onAction()
    if (action.accelerator != null) item.accelerator = action.accelerator
    item
