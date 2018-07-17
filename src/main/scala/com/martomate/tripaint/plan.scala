package com.martomate.tripaint

object plan {
  object model {
    object image {
      /** An entire image (on file). Can be referenced in several ImageSections */
      trait ImageStorage

      /** Part of an image, like a view of the image */
      trait ImageSection {
        val storage: ImageStorage
        /** Location in the image */
        val location: (Int, Int)
      }

      /** An image as part of an image collection */
      trait Image {
        val image: ImageSection
        val active: Boolean
        val rotation: Double
      }

      trait ImageCollection {
        val images: Seq[Image]

        /** Reach into all active images starting at start. Flood fill search. */
        def search(start: (Int, Int), pred: Int => Boolean): Seq[(Int, Int)]
      }
    }
  }

  /**
    * read from model
    * register listeners on model
    */
  object view {
    import model.image._

    trait ImagePane {
      val image: Image
    }

    trait ImageCollectionPane {
      val collection: ImageCollection
      val imagePanes: Seq[ImagePane]
    }
  }
}
