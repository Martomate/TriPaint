package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.coords.TriangleCoords
import javafx.scene.input.{MouseEvent, ScrollEvent}
import scalafx.beans.property._
import scalafx.scene.layout.Pane

trait TriImage extends ITriImage {
  def content: ImageContent
  def imagePane: ImagePaneView

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
