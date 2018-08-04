package com.martomate.tripaint

import com.martomate.tripaint.image.format.StorageFormat
import com.martomate.tripaint.image.storage.ImageStorage
import scalafx.scene.canvas.Canvas

object plan {
  private object model {
    object image {
      object format {// The format system is not really used at the moment
        trait SimpleStorageFormat extends StorageFormat // current format

        trait SubImageStorageFormat extends StorageFormat // new format where every quadrant can be read as an image
      }
    }
  }

  /**
    * read from model
    * register listeners on model
    */
  private object view {// TODO: this is outdated
    trait ImagePane {
      val image: ImageStorage
      val canvas: Canvas
    }

    trait ImageGridPane {
//      val collection: ImageGrid
      val imagePanes: Seq[ImagePane]
    }
  }
}
