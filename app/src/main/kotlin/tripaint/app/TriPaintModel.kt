package tripaint.app

import tripaint.grid.ImageGrid
import tripaint.image.ImagePool

class TriPaintModel(val fileSystem: FileSystem, initialImageSize: Int) {
    val imagePool: ImagePool = ImagePool()
    val imageGrid: ImageGrid = ImageGrid(initialImageSize)

    companion object {
        fun create(): TriPaintModel = TriPaintModel(FileSystem.create(), -1)

        fun createNull(
            imageSize: Int,
            fileSystemArgs: FileSystem.NullArgs = FileSystem.NullArgs()
        ): TriPaintModel = TriPaintModel(FileSystem.createNull(fileSystemArgs), imageSize)
    }
}
