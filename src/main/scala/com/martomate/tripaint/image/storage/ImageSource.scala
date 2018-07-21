package com.martomate.tripaint.image.storage

import com.martomate.tripaint.Listenable
import scalafx.scene.paint.Color

trait ImageSource extends Listenable[ImageSourceListener] {
  def apply(x: Int, y: Int): Color
  def update(x: Int, y: Int, col: Color): Unit

  /**
    * Saves this image
    *
    * @return true if the image was saved, false otherwise
    */
  def save(): Boolean

  final def changed: Boolean = hasChanged
  protected final var hasChanged: Boolean = false

  final def apply(c: (Int, Int)): Color = apply(c._1, c._2)
  final def update(c: (Int, Int), col: Color): Unit = update(c._1, c._2, col)
}
