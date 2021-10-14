package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.TriangleCoords

case class PixelChange(coords: TriangleCoords, before: Color, after: Color)
