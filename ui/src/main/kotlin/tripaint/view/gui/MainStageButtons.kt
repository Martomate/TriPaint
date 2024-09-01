package tripaint.view.gui

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import tripaint.view.MenuBarAction

object MainStageButtons {
    val New: MenuBarAction = MenuBarAction(
        "New",
        "new",
        KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
    UIAction.New
    )
    val Open: MenuBarAction = MenuBarAction(
        "Open",
        "open",
        KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
    UIAction.Open
    )
    val OpenHexagon: MenuBarAction = MenuBarAction(
        "Open hexagon",
        "open_hexagon",
        KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
    UIAction.OpenHexagon
    )
    val Save: MenuBarAction = MenuBarAction(
        "Save",
        "save",
        KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
    UIAction.Save
    )
    val SaveAs: MenuBarAction = MenuBarAction(
        "Save As",
        accelerator =
        KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
    action = UIAction.SaveAs
    )
    val Exit: MenuBarAction = MenuBarAction("Exit", action = UIAction.Exit)
    val Undo: MenuBarAction = MenuBarAction(
        "Undo",
        "undo",
        KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
    UIAction.Undo
    )
    val Redo: MenuBarAction = MenuBarAction(
        "Redo",
        "redo",
        KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
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