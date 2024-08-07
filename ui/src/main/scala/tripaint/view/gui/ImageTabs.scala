package tripaint.view.gui

import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.view.image.TriImageForPreview

import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane

import scala.collection.mutable

class ImageTabs(imagePool: ImagePool, requestImageRemoval: GridCell => Unit) extends TilePane {
  this.setMaxWidth(TriImageForPreview.previewSize)

  private val imageTabMap: mutable.Map[GridCell, StackPane] = mutable.Map.empty

  def onImageGridEvent(event: ImageGrid.Event): Unit = {
    event match {
      case ImageGrid.Event.ImageAdded(image) =>
        val tab = ImageTabPane(image, requestImageRemoval, imagePool)
        this.getChildren.add(tab)
        imageTabMap(image) = tab
      case ImageGrid.Event.ImageRemoved(image) =>
        imageTabMap.remove(image) match {
          case Some(pane) => this.getChildren.remove(pane)
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
    imageGrid.trackChanges(tilePane.onImageGridEvent(_))
    tilePane
  }
}
