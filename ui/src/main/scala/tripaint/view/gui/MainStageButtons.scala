package tripaint.view.gui

import tripaint.view.MenuBarAction

import javafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}

object MainStageButtons {
  val New: MenuBarAction = MenuBarAction(
    "New",
    "new",
    new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
    UIAction.New
  )
  val Open: MenuBarAction = MenuBarAction(
    "Open",
    "open",
    new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
    UIAction.Open
  )
  val OpenHexagon: MenuBarAction = MenuBarAction(
    "Open hexagon",
    "open_hexagon",
    new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
    UIAction.OpenHexagon
  )
  val Save: MenuBarAction = MenuBarAction(
    "Save",
    "save",
    new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
    UIAction.Save
  )
  val SaveAs: MenuBarAction = MenuBarAction(
    "Save As",
    accelerator =
      new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
    action = UIAction.SaveAs
  )
  val Exit: MenuBarAction = MenuBarAction("Exit", action = UIAction.Exit)
  val Undo: MenuBarAction = MenuBarAction(
    "Undo",
    "undo",
    new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
    UIAction.Undo
  )
  val Redo: MenuBarAction = MenuBarAction(
    "Redo",
    "redo",
    new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
    UIAction.Redo
  )
  val Cut: MenuBarAction = MenuBarAction("Cut", "cut")
  val Copy: MenuBarAction = MenuBarAction("Copy", "copy")
  val Paste: MenuBarAction = MenuBarAction("Paste", "paste")
  val Move: MenuBarAction = MenuBarAction("Move", "move")
  val Scale: MenuBarAction = MenuBarAction("Scale", "scale")
  val Rotate: MenuBarAction = MenuBarAction("Rotate", "rotate")
  val Blur: MenuBarAction = MenuBarAction("Blur", action = UIAction.Blur)
  val MotionBlur: MenuBarAction =
    MenuBarAction("Motion blur", action = UIAction.MotionBlur)
  val RandomNoise: MenuBarAction =
    MenuBarAction("Random noise", action = UIAction.RandomNoise)
  val Scramble: MenuBarAction = MenuBarAction("Scramble", action = UIAction.Scramble)
  val Cell: MenuBarAction = MenuBarAction("Cell", action = UIAction.Cell)
}
