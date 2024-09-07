package tripaint.app

import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.StorageCoords
import tripaint.effects.Effect
import tripaint.grid.*
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.image.format.StorageFormat
import tripaint.util.CachedLoader
import tripaint.view.FileOpenSettings
import tripaint.view.FileSaveSettings
import java.io.File

object Actions {
    fun save(
        imageGrid: ImageGrid,
        imagePool: ImagePool,
        images: List<GridCell>,
        fileSystem: FileSystem,
        askForSaveFile: (GridCell) -> File?,
        askForFileSaveSettings: (File, GridCell) -> FileSaveSettings?,
        imageSaveCollisionHandler: ImageSaveCollisionHandler
    ): Boolean {
        return images.filter { im -> !trySaveImage(imageGrid, imagePool, im.storage, fileSystem) }
            .all { im ->
                trySaveImage(imageGrid, imagePool, im.storage, fileSystem) ||
                        saveAs(imageGrid, imagePool, im, fileSystem,
                            askForSaveFile,
                            askForFileSaveSettings,
                            imageSaveCollisionHandler
                        )
            }
    }

    private fun trySaveImage(
        imageGrid: ImageGrid,
        imagePool: ImagePool,
        image: ImageStorage,
        fileSystem: FileSystem
    ): Boolean {
        val (loc, info) = imagePool.getSaveLocationAndInfo(image)
        return if (loc != null && info != null) {
            ImageSaver.saveImage(imageGrid, image, fileSystem, loc, info)
        } else {
            false
        }
    }

    fun saveAs(imageGrid: ImageGrid, imagePool: ImagePool, image: GridCell, fileSystem: FileSystem,
        askForSaveFile: (GridCell) -> File?,
        askForFileSaveSettings: (File, GridCell) -> FileSaveSettings?,
        imageSaveCollisionHandler: ImageSaveCollisionHandler
    ): Boolean {
        val file = askForSaveFile(image)
        val didMoveOpt = if (file != null) {
            val settings = askForFileSaveSettings(file, image)
            if (settings != null) {
                val location = ImagePool.SaveLocation(file, settings.offset)
                val info = ImagePool.SaveInfo(settings.format)
                imageGrid.setImageSource(image.storage, location, info, imagePool, imageSaveCollisionHandler)
            } else null
        } else null

        val didMove = didMoveOpt ?: false

        return if (didMove) {
            val saved = trySaveImage(imageGrid, imagePool, image.storage, fileSystem)
            if (!saved) println("Image could not be saved!!")
            saved
        } else false
    }

    fun createNewImage(imageGrid: ImageGrid, backgroundColor: Color, coords: GridCoords) {
        val storage = ImageStorage.fill(imageGrid.imageSize, backgroundColor)
        imageGrid.set(GridCell(coords, storage))
    }

    fun openImage(
        fileSystem: FileSystem,
        imagePool: ImagePool,
        imageGrid: ImageGrid,
        file: File,
        fileOpenSettings: FileOpenSettings,
        whereToPutImage: GridCoords
    ) {
        val (offset, format) = fileOpenSettings
        val location = ImagePool.SaveLocation(file, offset)
        val imageSize = imageGrid.imageSize

        CachedLoader.apply(
            cached = { imagePool.imageAt(location) },
            load = { loadImageFromFile(location, format, imageSize, fileSystem) }
        ).onSuccess {
            val (image, found) = it
            if (!found) imagePool.set(image, location, ImagePool.SaveInfo(format))
            imageGrid.set(GridCell(whereToPutImage, image))
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun loadImageFromFile(
        location: ImagePool.SaveLocation,
        format: StorageFormat,
        imageSize: Int,
        fileSystem: FileSystem
    ): Result<ImageStorage> {
        val im = fileSystem.readImage(location.file) ?: return Result.failure(RuntimeException("no such image"))
        return ImageStorage.fromRegularImage(im, location.offset, format, imageSize)
    }

    fun openHexagon(
        fileSystem: FileSystem, imagePool: ImagePool, imageGrid: ImageGrid,
        file: File, fileOpenSettings: FileOpenSettings, coords: GridCoords
    ) {
        val imageSize = imageGrid.imageSize
        val (offset, format) = fileOpenSettings

        fun coordOffset(idx: Int): Pair<Int, Int> {
            return when (idx) {
                0 -> Pair(0, 0)
                1 -> Pair(-1, 0)
                2 -> Pair(-2, 0)
                3 -> Pair(-1, -1)
                4 -> Pair(0, -1)
                5 -> Pair(1, -1)
                else -> throw RuntimeException()
            }
        }

        for (idx in 0 until 6) {
            val imageOffset = StorageCoords.from(offset.x + idx * imageSize, offset.y)

            val off = coordOffset(idx)
            val whereToPutImage = GridCoords.from(coords.x + off.first, coords.y + off.second)

            openImage(fileSystem, imagePool, imageGrid, file, FileOpenSettings(imageOffset, format), whereToPutImage)
        }
    }

    fun applyEffect(imageGrid: ImageGrid, effect: Effect) {
        val grid = imageGrid
        val images = grid.selectedImages()

        val before = images.map { im -> im.storage.allPixels().map { pix -> im.storage.getColor(pix) } }

        effect.action(images.map { it.coords }, grid)

        val after = images.map { im -> im.storage.allPixels().map { pix -> im.storage.getColor(pix) } }

        val changes: MutableMap<GridCoords, ImageChange> = mutableMapOf()
        for (here in images.indices) {
            val image = images[here]
            val allPixels = image.storage.allPixels()

            val changeBuilder = ImageChange.Builder()
            for (neigh in allPixels.indices) {
                if (before[here][neigh] != after[here][neigh]) {
                    changeBuilder.addChange(allPixels[neigh], before[here][neigh], after[here][neigh])
                }
            }

            if (changeBuilder.nonEmpty()) {
                changes[image.coords] = changeBuilder.done(image.storage)
            }
        }

        for ((_, change) in changes) {
            change.undo()
        }

        grid.performChange(ImageGridChange(changes))
    }
}