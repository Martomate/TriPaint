package com.martomate.tripaint.model.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords

import java.awt.image.BufferedImage
import java.util

class RegularImage private (val width: Int, val height: Int, private val pixels: Array[Int]) {
  def getColor(x: Int, y: Int): Color = {
    require(x >= 0)
    require(y >= 0)
    require(x < width)
    require(y < height)

    Color.fromInt(pixels(x + y * width))
  }

  def setColor(x: Int, y: Int, color: Color): Unit = {
    require(x >= 0)
    require(y >= 0)
    require(x < width)
    require(y < height)

    pixels(x + y * width) = color.toInt
  }

  def pasteImage(offset: StorageCoords, image: RegularImage): Unit = {
    require(offset.x + image.width <= width)
    require(offset.y + image.height <= height)

    val StorageCoords(dx, dy) = offset

    for {
      y <- 0 until image.height
      x <- 0 until image.width
    } pixels(dx + x + (dy + y) * width) = image.pixels(x + y * image.width)
  }

  def toBufferedImage: BufferedImage = {
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    image.setRGB(0, 0, width, height, pixels, 0, width)
    image
  }

  override def equals(obj: Any): Boolean = obj match {
    case other: RegularImage =>
      if (width != other.width || height != other.height) false
      else util.Arrays.equals(pixels, other.pixels)
    case _ => false
  }

  override def toString: String = {
    pixels
      .grouped(width)
      .map(_.map(Integer.toHexString).mkString(", "))
      .mkString("\n")
  }
}

object RegularImage {
  def ofSize(width: Int, height: Int): RegularImage = {
    new RegularImage(width, height, Array.ofDim(width * height))
  }

  def fromBufferedImage(image: BufferedImage): RegularImage = {
    val w = image.getWidth()
    val h = image.getHeight()

    new RegularImage(w, h, image.getRGB(0, 0, w, h, null, 0, w))
  }

  def fill(width: Int, height: Int, color: Color): RegularImage = {
    val image = ofSize(width, height)
    for {
      y <- 0 until height
      x <- 0 until width
    } image.setColor(x, y, color)
    image
  }
}
