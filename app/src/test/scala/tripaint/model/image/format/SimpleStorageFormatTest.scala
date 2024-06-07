package tripaint.model.image.format

class SimpleStorageFormatTest extends StorageFormatTest {
  test("transformToStorage should transform correctly") {
    triToStorage(0, 0)(0, 0)
    triToStorage(0, 10)(0, 10)
    triToStorage(10, 10)(10, 10)
    triToStorage(20, 10)(10, 0)
  }

  test("transformFromStorage should transform correctly") {
    storageToTri(0, 0)(0, 0)
    storageToTri(0, 10)(0, 10)
    storageToTri(10, 10)(10, 10)
    storageToTri(10, 0)(20, 10)
  }

  def triToStorage(from: (Int, Int))(to: (Int, Int)): Unit = {
    assertEquals(make.transform(trCoords(from)), stCoords(to))
  }

  def storageToTri(from: (Int, Int))(to: (Int, Int)): Unit = {
    assertEquals(make.reverse(stCoords(from)), trCoords(to))
  }

  def make: StorageFormat = SimpleStorageFormat
}
