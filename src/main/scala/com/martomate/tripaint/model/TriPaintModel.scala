package com.martomate.tripaint.model

import com.martomate.tripaint.model.format.SimpleStorageFormat
import com.martomate.tripaint.model.grid.{ImageGrid, ImageGridImplOld}
import com.martomate.tripaint.model.pool.{ImagePool, ImagePoolImpl, ImageSaveCollisionHandler}
import com.martomate.tripaint.model.save.{ImageSaver, ImageSaverToFile}
import com.martomate.tripaint.model.storage.ImageStorageImpl

class TriPaintModel {
  val imageGrid: ImageGrid = new ImageGridImplOld(-1)
  val imagePool: ImagePool = new ImagePoolImpl(ImageStorageImpl)
  val imageSaver: ImageSaver = new ImageSaverToFile(new SimpleStorageFormat)
}
