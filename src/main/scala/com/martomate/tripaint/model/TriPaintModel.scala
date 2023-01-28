package com.martomate.tripaint.model

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.grid.ImageGrid
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.model.image.save.ImageSaverToFile

class TriPaintModel(val fileSystem: FileSystem) {
  val imagePool: ImagePool = new ImagePool()
  val imageGrid: ImageGrid = new ImageGrid(-1)
  imagePool.addListener(imageGrid)
}

object TriPaintModel {
  def create(): TriPaintModel = new TriPaintModel(FileSystem.create())

  def createNull(fileSystemArgs: FileSystem.NullArgs = new FileSystem.NullArgs()): TriPaintModel =
    new TriPaintModel(FileSystem.createNull(fileSystemArgs))
}
