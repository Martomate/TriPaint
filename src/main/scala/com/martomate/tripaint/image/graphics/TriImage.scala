package com.martomate.tripaint.image.graphics

import com.martomate.tripaint.image.content.ImageContent
import com.martomate.tripaint.image.coords.TriangleCoords
import com.martomate.tripaint.image.effects.Effect
import javafx.scene.input.{MouseEvent, ScrollEvent}
import scalafx.beans.property._
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

object TriImage {
  val previewSize = 64

  def apply(content: ImageContent, imagePane: ImageGridView) = TriImageImpl(content, imagePane)
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

  def applyEffect(effect: Effect): Unit

  def pane: Pane
}
