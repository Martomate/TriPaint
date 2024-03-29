package tripaint.model.image.format

import tripaint.model.coords.{StorageCoords, TriangleCoords}

/** This recursive format is designed to map small triangles into small squares If the storage image
  * is scaled up by a factor of 2, the corresponding triangle image looks the same.
  *
  * This is achieved by dividing the image into four parts, flipping the center upside down, and
  * then storing these parts as squares as shown below. If the parts are not 2x2 the process is
  * repeated recursively.
  *
  * {{{
  *           1
  *        2  3  4
  *     5  6  7  8  9
  *  10 11 12 13 14 15 16
  *
  *  1  4  9  16
  *  2  3  14 15
  *  5  12 7  8
  *  10 11 6  13
  * }}}
  *
  * If we consider a triangle made of four subtriangles, the transformation uses the following
  * recursion. Fa means that the transformation F is applied to the triangle a, * means 'rotate 180
  * degrees'.
  * {{{
  *      a
  *    b c d
  *
  *    Fa Fd
  *    Fb (Fc)*
  * }}}
  */
object RecursiveStorageFormat extends StorageFormat {
  override def transform(coords: TriangleCoords): StorageCoords =
    if coords.y == 0
    then StorageCoords(0, 0)
    else
      val floor = Integer.highestOneBit(coords.y)
      val rest = coords.y - floor

      if coords.x <= 2 * rest then // left
        val subCoords = transform(TriangleCoords(coords.x, rest))
        StorageCoords(subCoords.x, floor + subCoords.y)
      else if coords.x >= 2 * floor then // right
        val subCoords = transform(TriangleCoords(coords.x - 2 * floor, rest))
        StorageCoords(floor + subCoords.x, subCoords.y)
      else // center
        val subCoords = transform(TriangleCoords(2 * floor - 1 - coords.x, floor - 1 - rest))
        StorageCoords(2 * floor - 1 - subCoords.x, 2 * floor - 1 - subCoords.y)

  override def reverse(coords: StorageCoords): TriangleCoords =
    if coords.x == 0 && coords.y == 0
    then TriangleCoords(0, 0)
    else
      val floor = math.max(Integer.highestOneBit(coords.x), Integer.highestOneBit(coords.y))
      val restX = coords.x - floor
      val restY = coords.y - floor

      if coords.x >= floor && coords.y >= floor then // center
        val subCoords = reverse(StorageCoords(floor - 1 - restX, floor - 1 - restY))
        TriangleCoords(2 * floor - 1 - subCoords.x, 2 * floor - 1 - subCoords.y)
      else if coords.x >= floor then // right
        val subCoords = reverse(StorageCoords(restX, coords.y))
        TriangleCoords(2 * floor + subCoords.x, floor + subCoords.y)
      else // left
        val subCoords = reverse(StorageCoords(coords.x, restY))
        TriangleCoords(subCoords.x, floor + subCoords.y)
}
