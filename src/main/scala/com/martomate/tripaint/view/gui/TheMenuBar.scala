package com.martomate.tripaint.view.gui

import scalafx.scene.control.{Menu, MenuBar, MenuItem, SeparatorMenuItem}

class TheMenuBar(controls: MainStageButtons) extends MenuBar {

  private def makeMenu(text: String, menuItems: MenuItem*): Menu = {
    val menu = new Menu(text)
    menu.items = menuItems
    menu
  }

  private val menu_file = makeMenu("File",
    controls.New.menuItem,
    controls.Open.menuItem,
    controls.OpenHexagon.menuItem,
    new SeparatorMenuItem,
    controls.Save.menuItem,
    controls.SaveAs.menuItem,
    new SeparatorMenuItem,
    controls.Exit.menuItem
  )

  private val menu_edit = makeMenu("Edit",
    controls.Undo.menuItem,
    controls.Redo.menuItem,
    new SeparatorMenuItem,
    controls.Cut.menuItem,
    controls.Copy.menuItem,
    controls.Paste.menuItem,
  )

  private val menu_organize = makeMenu("Organize",
    controls.Move.menuItem,
    controls.Scale.menuItem,
    controls.Rotate.menuItem
  )

  private val menu_effects = makeMenu("Effects",
    controls.Blur.menuItem,
    controls.MotionBlur.menuItem,
    controls.RandomNoise.menuItem,
    controls.Scramble.menuItem
  )

  useSystemMenuBar = true
  menus = Seq(menu_file, menu_edit, menu_organize, menu_effects)
}
