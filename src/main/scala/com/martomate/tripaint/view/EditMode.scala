package com.martomate.tripaint.view

import javafx.scene.input.{KeyCode, KeyCodeCombination}
import scalafx.beans.property.{ReadOnlyObjectProperty, ReadOnlyObjectWrapper}
import scalafx.scene.control.{ToggleButton, Tooltip}
import scalafx.scene.image.ImageView

class EditMode(
    imagePath: String,
    val tooltipText: String,
    val shortCut: KeyCodeCombination = null
) {
  val toolboxButton: ToggleButton = {
    val t = new ToggleButton(null, new ImageView("icons/editmodes/" + imagePath + ".png"))
    t.tooltip = new Tooltip(s"$tooltipText\n(Shortcut: $shortCut)")
    t.onAction = _ => select()
    t
  }

  EditMode._modes :+= this

  def select(): Unit = {
    EditMode.deselectAll()
    toolboxButton.selected = true

    EditMode.currentMode = this
  }
}

object EditMode {
  private var _modes = Vector.empty[EditMode]

  def modes: Vector[EditMode] = _modes

  private val _currentMode = new ReadOnlyObjectWrapper[EditMode](null, null)

  private def currentMode_=(mode: EditMode): Unit = _currentMode.value = mode

  def currentMode: EditMode = _currentMode.value

  val Select = new EditMode("select", "Select", new KeyCodeCombination(KeyCode.S))
  val Draw = new EditMode("draw", "Draw", new KeyCodeCombination(KeyCode.P))
  val Fill = new EditMode("fill", "Fill", new KeyCodeCombination(KeyCode.F))
  val PickColor = new EditMode("pickColor", "Pick Color", new KeyCodeCombination(KeyCode.K))
  val Organize = new EditMode("organize", "Organize", new KeyCodeCombination(KeyCode.O))

  Draw.select()

  private def deselectAll(): Unit = EditMode.modes.foreach(_.toolboxButton.selected = false)
}
