package com.martomate.tripaint.model

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.grid.ImageGrid
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.model.image.save.ImageSaverToFile

class TriPaintModel {
  val imageGrid: ImageGrid = new ImageGrid(-1)
  val imagePool: ImagePool = new ImagePool()
  val imageSaver: ImageSaverToFile = new ImageSaverToFile()
  val fileSystem: FileSystem = FileSystem.create()
}
