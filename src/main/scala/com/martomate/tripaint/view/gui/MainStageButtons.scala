package com.martomate.tripaint.view.gui

import com.martomate.tripaint.view.{MenuBarAction, TriPaintViewListener}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}

class MainStageButtons(control: TriPaintViewListener) {
  val New: MenuBarAction = MenuBarAction("New", "new", new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown), control.action_new())
  val Open: MenuBarAction = MenuBarAction("Open", "open", new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown), control.action_open())
  val OpenHexagon: MenuBarAction = MenuBarAction("Open hexagon", "open_hexagon", new KeyCodeCombination(KeyCode.H, KeyCombination.ControlDown), control.action_openHexagon())
  val Save: MenuBarAction = MenuBarAction("Save", "save", new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown), control.action_save())
  val SaveAs: MenuBarAction = MenuBarAction("Save As", accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown, KeyCombination.ShiftDown), onAction = control.action_saveAs())
  val Exit: MenuBarAction = MenuBarAction("Exit", onAction = control.action_exit())
  val Undo: MenuBarAction = MenuBarAction("Undo", "undo", new KeyCodeCombination(KeyCode.Z, KeyCombination.ControlDown), control.action_undo())
  val Redo: MenuBarAction = MenuBarAction("Redo", "redo", new KeyCodeCombination(KeyCode.Y, KeyCombination.ControlDown), control.action_redo())
  val Cut: MenuBarAction = MenuBarAction("Cut", "cut")
  val Copy: MenuBarAction = MenuBarAction("Copy", "copy")
  val Paste: MenuBarAction = MenuBarAction("Paste", "paste")
  val Move: MenuBarAction = MenuBarAction("Move", "move")
  val Scale: MenuBarAction = MenuBarAction("Scale", "scale")
  val Rotate: MenuBarAction = MenuBarAction("Rotate", "rotate")
  val Blur: MenuBarAction = MenuBarAction("Blur", onAction = control.action_blur())
  val MotionBlur: MenuBarAction = MenuBarAction("Motion blur", onAction = control.action_motionBlur())
  val RandomNoise: MenuBarAction = MenuBarAction("Random noise", onAction = control.action_randomNoise())
  val Scramble: MenuBarAction = MenuBarAction("Scramble", onAction = control.action_scramble())
}
