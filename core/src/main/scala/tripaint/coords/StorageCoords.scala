package tripaint.coords

case class StorageCoords(value: Long) extends AnyVal {
  inline def x: Int = (value >> 32).toInt
  inline def y: Int = value.toInt
}

object StorageCoords {
  inline def apply(x: Int, y: Int): StorageCoords = {
    require(x >= 0)
    require(y >= 0)
    new StorageCoords(x.toLong << 32 | y)
  }
}
