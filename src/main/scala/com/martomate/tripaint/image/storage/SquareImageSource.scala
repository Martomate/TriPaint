package com.martomate.tripaint.image.storage

import scalafx.scene.paint.Color

import scala.util.Try

class SquareImageSource private(source: ImageSource, xOff: Int, yOff: Int, val s: Int) extends ImageSourceLense(source) {
  override protected def encode(x: Int, y: Int): (Int, Int) = (x + xOff, y + yOff)
  override protected def decode(x: Int, y: Int): (Int, Int) = (x - xOff, y - yOff)
  override protected def inLense(x: Int, y: Int): Boolean = x >= xOff && x < xOff + s && y >= yOff && y < yOff + s

  def apply(index: Int): Color = apply(index % s, index / s)
  def update(index: Int, col: Color): Unit = update(index % s, index / s, col)

  override def width: Int = s
  override def height: Int = s
}

object SquareImageSource {
  def apply(source: ImageSource, xOff: Int, yOff: Int, s: Int): Try[SquareImageSource] = Try {
    if (xOff < 0 || yOff < 0 || xOff + s > source.width || yOff + s > source.height)
      throw new IllegalArgumentException(s"An image with offset ($xOff, $yOff) and imageSize $s would reach out of the source image.")

    new SquareImageSource(source, xOff, yOff, s)
  }
}