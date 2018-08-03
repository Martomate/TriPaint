package com.martomate.tripaint

object plan {
  import com.martomate.tripaint.image.SaveLocation
  import com.martomate.tripaint.plan.model.image2.storage.ImageStorage
  import scalafx.scene.canvas.Canvas
  import scalafx.scene.paint.Color

  private object model {
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
          val format: StorageFormat

          def save(image: ImageStorage, location: SaveLocation): Boolean
        }
      }

      object storage {
        import coords._
        import save._

        trait ImageStorageListener {
          def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit
        }

        trait ImageStorage {
          val imageSize: Int

          def apply(coords: TriangleCoords): Color

          def update(coords: TriangleCoords, col: Color): Unit
        }

        trait ImagePool extends Listenable[ImagePoolListener] {
          val expert: ImagePoolCollisionExpert

          def get(newLocation: SaveLocation): ImageStorage
          def set(newLocation: SaveLocation, imageStorage: ImageStorage): Unit
          protected def remove(imageStorage: ImageStorage): Unit

          def move(image: ImageStorage, newLocation: SaveLocation): Unit = {
            val currentImage = get(newLocation)
            if (currentImage != image) {
              expert.shouldReplaceImage(currentImage, image, newLocation) match {
                case Some(replace) =>
                  if (replace) {
                    remove(currentImage)
                    set(newLocation, image)
                    notifyListeners(_.onImageReplaced(currentImage, image, newLocation))
                  } else {
                    remove(image)
                    notifyListeners(_.onImageReplaced(image, currentImage, newLocation))
                  }
                case None =>
              }
            }
          }

          def save(image: ImageStorage, saver: ImageSaver): Boolean
        }

        trait ImagePoolListener {
          def onImageReplaced(oldImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Unit
        }

        trait ImagePoolCollisionExpert {
          def shouldReplaceImage(currentImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Option[Boolean]
        }

        trait ImageChangeTracker {
          val storage: ImageStorage
          val pool: ImagePool

          def changed: Boolean
        }

        trait ImageContent {
          val storage: ImageStorage

          var editable: Boolean

          val changeTracker: ImageChangeTracker
        }
      }
    }
  }

  /**
    * read from model
    * register listeners on model
    */
  private object view {
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
