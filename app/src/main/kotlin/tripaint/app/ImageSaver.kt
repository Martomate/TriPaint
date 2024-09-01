package tripaint.app

import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.image.RegularImage

object ImageSaver {
    fun saveImage(
        imageGrid: ImageGrid,
        image: ImageStorage,
        fileSystem: FileSystem,
        loc: ImagePool.SaveLocation,
        info: ImagePool.SaveInfo
    ): Boolean {
        val oldImage = fileSystem.readImage(loc.file)

        val imageToSave = image.toRegularImage(info.format)
        val newImage = RegularImage.fromBaseAndOverlay(oldImage, imageToSave, loc.offset)

        val didWrite = fileSystem.writeImage(newImage, loc.file)

        if (didWrite) {
            imageGrid.findByStorage(image)?.setImageSaved()
        }
        return didWrite
    }
}
