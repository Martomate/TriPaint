package com.martomate.tripaint.image.storage

import scalafx.scene.paint.Color

import scala.util.Try

class SquareImageLense private(source: ImageSource, xOff: Int, yOff: Int, _sideLength: Int) extends ImageLense(source, _sideLength) {
  override protected def encode(x: Int, y: Int): (Int, Int) = (x + xOff, y + yOff)
  override protected def decode(x: Int, y: Int): (Int, Int) = (x - xOff, y - yOff)
  override protected def inLense(x: Int, y: Int): Boolean = x >= xOff && x < xOff + sideLength && y >= yOff && y < yOff + sideLength

  def apply(index: Int): Color = apply(index % sideLength, index / sideLength)
  def update(index: Int, col: Color): Unit = update(index % sideLength, index / sideLength, col)
}

object SquareImageLense {
  def apply(source: ImageSource, xOff: Int, yOff: Int, s: Int): Try[SquareImageLense] = Try {
    if (xOff < 0 || yOff < 0 || xOff + s > source.width || yOff + s > source.height)
      throw new IllegalArgumentException(s"An image with offset ($xOff, $yOff) and imageSize $s would reach out of the source image.")

    new SquareImageLense(source, xOff, yOff, s)
  }
}