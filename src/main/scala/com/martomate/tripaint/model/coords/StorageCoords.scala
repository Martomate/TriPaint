package com.martomate.tripaint.model.coords

case class StorageCoords(x: Int, y: Int) {
  require(x >= 0, "x must be positive")
  require(y >= 0, "y must be positive")
}
