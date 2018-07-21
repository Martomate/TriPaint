package com.martomate.tripaint

import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

object plan {
  object model {
    object image {
      object storage {
        trait PixelCoordsInStorage {
          def x: Int
          def y: Int
        }

        /** An entire image (on file). Can be referenced in several ImageSections */
        trait ImageStorage {
          def apply(coords: PixelCoordsInStorage): Color
          def update(coords: PixelCoordsInStorage, color: Color): Unit

          def addListener(listener: PixelCoordsInStorage => Unit): Unit

          def hasChanged: Boolean
          def save(): Unit
        }
      }

      object triimage {
        import storage._

        trait PixelCoordsInImage {
          def x: Int
          def y: Int
          /** Calculated from x and y */
          def index: Int
        }

        /** Part of an image, like a view of the image */
        trait ImageSection {
          val storage: ImageStorage
          /** Location in the image */
          val location: (Int, Int)

          def apply(coords: PixelCoordsInImage): Color
          def update(coords: PixelCoordsInImage, color: Color): Unit

          def addListener(listener: PixelCoordsInImage => Unit): Unit
        }
      }

      object imagegrid {
        import triimage._

        trait ImageCoordsInGrid {
          def x: Int
          def y: Int
        }

        trait PixelCoords {
          val pix: PixelCoordsInImage
          val image: ImageCoordsInGrid
        }

        /** An image as part of an image grid */
        trait Image {
          val image: ImageSection
          val coords: ImageCoordsInGrid
          val active: Boolean

          /** Rotation in units of 120 deg relative to the default (0 deg, or 180 deg if it's upside down) */
          val rotation: Int

          /** In degrees (because of JavaFX!) */
          def actualRotation: Double = rotation * 120 + (coords.x % 2) * 180
        }

        trait ImageGrid {
          def images: Seq[Image]

          def apply(coords: ImageCoordsInGrid): Image
          def update(coords: ImageCoordsInGrid, image: Image): Unit

          /** Reach into all active images starting at start. Flood fill search. */
          def search(start: PixelCoords, pred: Int => Boolean): Seq[PixelCoords]
        }
      }
    }
  }

  /**
    * read from model
    * register listeners on model
    */
  object view {
    import model.image.imagegrid._

    trait ImagePane {
      val image: Image
      val canvas: Canvas
    }

    trait ImageGridPane {
      val collection: ImageGrid
      val imagePanes: Seq[ImagePane]
    }
  }
}
