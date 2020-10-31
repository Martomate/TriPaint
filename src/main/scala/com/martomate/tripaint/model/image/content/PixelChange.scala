package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.coords.TriangleCoords
import scalafx.scene.paint.Color

case class PixelChange(coords: TriangleCoords, before: Color, after: Color)
