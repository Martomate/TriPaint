package com.martomate.tripaint.model

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.grid.{ImageGrid, ImageGridImplOld}
import com.martomate.tripaint.model.image.pool.{ImagePool, ImagePoolImpl}
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.ImageStorageImpl

class TriPaintModel {
  val imageGrid: ImageGrid = new ImageGridImplOld(-1)
  val imagePool: ImagePool = new ImagePoolImpl(ImageStorageImpl)
  val imageSaver: ImageSaverToFile = new ImageSaverToFile()
  val fileSystem: FileSystem = FileSystem.create()
}
