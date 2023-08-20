package com.martomate.tripaint.model

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.image.ImagePool

class TriPaintModel(val fileSystem: FileSystem) {
  val imagePool: ImagePool = new ImagePool()
  val imageGrid: ImageGrid = new ImageGrid(-1)
}

object TriPaintModel {
  def create(): TriPaintModel = new TriPaintModel(FileSystem.create())

  def createNull(fileSystemArgs: FileSystem.NullArgs = new FileSystem.NullArgs()): TriPaintModel =
    new TriPaintModel(FileSystem.createNull(fileSystemArgs))
}
