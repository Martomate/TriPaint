package com.martomate.tripaint.view.gui

import scalafx.scene.control.{Menu, MenuBar, MenuItem, SeparatorMenuItem}

object TheMenuBar:
  def create(controls: MainStageButtons): MenuBar =
    val menu_file = makeMenu(
      "File",
      controls.New.menuItem,
      controls.Open.menuItem,
      controls.OpenHexagon.menuItem,
      new SeparatorMenuItem,
      controls.Save.menuItem,
      controls.SaveAs.menuItem,
      new SeparatorMenuItem,
      controls.Exit.menuItem
    )

    val menu_edit = makeMenu(
      "Edit",
      controls.Undo.menuItem,
      controls.Redo.menuItem,
      new SeparatorMenuItem,
      controls.Cut.menuItem,
      controls.Copy.menuItem,
      controls.Paste.menuItem
    )

    val menu_organize =
      makeMenu(
        "Organize",
        controls.Move.menuItem,
        controls.Scale.menuItem,
        controls.Rotate.menuItem
      )

    val menu_effects = makeMenu(
      "Effects",
      controls.Blur.menuItem,
      controls.MotionBlur.menuItem,
      controls.RandomNoise.menuItem,
      controls.Scramble.menuItem
    )

    val menuBar = new MenuBar
    menuBar.useSystemMenuBar = true
    menuBar.menus = Seq(menu_file, menu_edit, menu_organize, menu_effects)
    menuBar

  private def makeMenu(text: String, menuItems: MenuItem*): Menu =
    val menu = new Menu(text)
    menu.items = menuItems
    menu
