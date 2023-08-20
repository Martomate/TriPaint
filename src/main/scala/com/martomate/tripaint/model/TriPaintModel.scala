package com.martomate.tripaint.model

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.image.ImagePool

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
