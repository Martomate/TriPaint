package tripaint.view.gui

import tripaint.model.ImageGrid
import tripaint.model.image.{GridCell, ImagePool}
import tripaint.view.image.TriImageForPreview

import scalafx.scene.layout.{StackPane, TilePane}

import scala.collection.mutable

class ImageTabs(imagePool: ImagePool, requestImageRemoval: GridCell => Unit) extends TilePane {
  maxWidth = TriImageForPreview.previewSize

  private val imageTabMap: mutable.Map[GridCell, StackPane] = mutable.Map.empty

  def onImageGridEvent(event: ImageGrid.Event): Unit = {
    event match {
      case ImageGrid.Event.ImageAdded(image) =>
        val tab = ImageTabPane(image, requestImageRemoval, imagePool)
        children.add(tab.delegate)
        imageTabMap(image) = tab
      case ImageGrid.Event.ImageRemoved(image) =>
        imageTabMap.remove(image) match {
          case Some(pane) => children.remove(pane.delegate)
          case None       =>
        }
      case _ =>
    }
  }
}

object ImageTabs {
  def fromImagePool(
      imageGrid: ImageGrid,
      imagePool: ImagePool,
      requestImageRemoval: GridCell => Unit
  ): TilePane = {
    val tilePane = new ImageTabs(imagePool, requestImageRemoval)
    imageGrid.trackChanges(tilePane.onImageGridEvent _)
    tilePane
  }
}
