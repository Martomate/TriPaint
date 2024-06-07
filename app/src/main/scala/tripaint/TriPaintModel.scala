package tripaint

import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.infrastructure.FileSystem

class TriPaintModel(val fileSystem: FileSystem, initialImageSize: Int) {
  val imagePool: ImagePool = new ImagePool()
  val imageGrid: ImageGrid = new ImageGrid(initialImageSize)
}

object TriPaintModel {
  def create(): TriPaintModel = new TriPaintModel(FileSystem.create(), -1)

  def createNull(
      imageSize: Int,
      fileSystemArgs: FileSystem.NullArgs = FileSystem.NullArgs()
  ): TriPaintModel =
    new TriPaintModel(FileSystem.createNull(fileSystemArgs), imageSize)
}
