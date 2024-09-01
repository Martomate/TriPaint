package tripaint.view

import tripaint.color.Color
import tripaint.coords.StorageCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageSaveCollisionHandler
import tripaint.image.format.StorageFormat
import java.io.File

interface TriPaintView : ImageSaveCollisionHandler {
    fun backgroundColor(): Color

    fun askForImageSize(): Int?
    fun askSaveBeforeClosing(images: List<GridCell>): Boolean?
    fun askForWhereToPutImage(): Pair<Int, Int>?

    fun askForSaveFile(image: GridCell): File?
    fun askForFileSaveSettings(file: File, image: GridCell): FileSaveSettings?

    fun askForFileToOpen(): File?
    fun askForFileOpenSettings(
        file: File,
        imageSize: Int,
        xCount: Int,
        yCount: Int
    ): FileOpenSettings?

    fun askForBlurRadius(): Int?
    fun askForMotionBlurRadius(): Int?
    fun askForRandomNoiseColors(): Pair<Color, Color>?

    fun close()
}

data class FileSaveSettings(val offset: StorageCoords, val format: StorageFormat)
data class FileOpenSettings(val offset: StorageCoords, val format: StorageFormat)
