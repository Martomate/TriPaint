package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.effects.Effect
import javafx.scene.input.{MouseEvent, ScrollEvent}
import scalafx.beans.property._
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

object TriImage {
  val previewSize = 64
}

trait TriImage extends ITriImage {
  def content: ImageContent
  def imagePane: ImageGridView

  def changed: Boolean
  def changedProperty: ReadOnlyBooleanProperty

  def onMouseReleased(e: MouseEvent): Unit
  def onScroll(e: ScrollEvent): Unit

  def undo(): Unit
  def redo(): Unit

  def coordsAt(x: Double, y: Double): TriangleCoords
  def drawAt(coords: TriangleCoords, color: Color): Unit

  def relocate(x: Double, y: Double): Unit

  def pane: Pane
}
