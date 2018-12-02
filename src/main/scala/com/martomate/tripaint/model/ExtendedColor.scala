package com.martomate.tripaint.model

import scalafx.scene.paint.Color

class ExtendedColor(val r: Double, val g: Double, val b: Double, val a: Double) {
  def +(c2: ExtendedColor) = new ExtendedColor(r + c2.r, g + c2.g, b + c2.b, a + c2.a)

  def -(c2: ExtendedColor) = new ExtendedColor(r - c2.r, g - c2.g, b - c2.b, a - c2.a)

  def *(d: Double) = new ExtendedColor(r * d, g * d, b * d, a * d)

  def /(d: Double) = new ExtendedColor(r / d, g / d, b / d, a / d)

  def toColor: Color = Color.color(clamp(r), clamp(g), clamp(b), clamp(a))

  private def clamp(v: Double, lo: Double = 0, hi: Double = 1) = math.min(math.max(v, lo), hi)
}

object ExtendedColor {
  import scala.language.implicitConversions

  implicit def colorToExtendedColor(c: Color): ExtendedColor = new ExtendedColor(c.red, c.green, c.blue, c.opacity)
}
