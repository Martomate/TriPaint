package com.martomate.tripaint

import com.martomate.tripaint.image.storage.SaveLocation
import com.martomate.tripaint.plan.model.image2.coords.TriangleCoords
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

    object image2 {
      object coords {
        trait TriangleCoords {
          def x: Int

          def y: Int
        }

        trait StorageCoords {
          def x: Int

          def y: Int
        }
      }

      object format {
        import coords._

        trait StorageFormat {
          def transformToStorage(coords: TriangleCoords): StorageCoords

          def transformFromStorage(coords: StorageCoords): TriangleCoords
        }

        trait SimpleStorageFormat extends StorageFormat // current format

        trait SubImageStorageFormat extends StorageFormat // new format where every quadrant can be read as an image
      }

      object save {
        import format._
        import storage._

        trait ImageSaver {
          def save(image: ImageStorage, saveInfo: ImageSaveInfo): Boolean
        }

        trait ImageSaveInfo {
          val saveLocation: SaveLocation
          val format: StorageFormat
        }
      }

      object storage {
        import save._

        trait ImageStorageListener {
          def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit
        }

        trait ImageStorage {
          val imageSize: Int

          def apply(coords: TriangleCoords): Color

          def update(coords: TriangleCoords, col: Color): Unit
        }

        trait TriImage {
          val storage: ImageStorage

          var enabled: Boolean

          def changed: Boolean

          var saveInfo: ImageSaveInfo

          def save(saver: ImageSaver): Boolean = saver.save(storage, saveInfo)
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
