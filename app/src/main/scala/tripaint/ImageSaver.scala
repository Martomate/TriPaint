package tripaint

import tripaint.image.{ImageStorage, RegularImage}
import tripaint.infrastructure.FileSystem
import tripaint.model.ImageGrid
import tripaint.model.image.ImagePool.{SaveInfo, SaveLocation}

object ImageSaver {
  def saveImage(
      imageGrid: ImageGrid,
      image: ImageStorage,
      fileSystem: FileSystem,
      loc: SaveLocation,
      info: SaveInfo
  ): Boolean = {
    val oldImage = fileSystem.readImage(loc.file)

    val imageToSave = image.toRegularImage(info.format)
    val newImage = RegularImage.fromBaseAndOverlay(oldImage, imageToSave, loc.offset)

    val didWrite = fileSystem.writeImage(newImage, loc.file)

    if didWrite then {
      for {
        im <- imageGrid.findByStorage(image)
      } do {
        im.setImageSaved()
      }
    }
    didWrite
  }
}
