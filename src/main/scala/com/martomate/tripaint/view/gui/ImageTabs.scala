package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.grid.ImageGrid
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.view.TriPaintViewListener
import com.martomate.tripaint.view.image.TriImageForPreview
import scalafx.scene.layout.{StackPane, TilePane}

import scala.collection.mutable

class ImageTabs(imagePool: ImagePool, requestImageRemoval: ImageContent => Unit) extends TilePane:
  maxWidth = TriImageForPreview.previewSize

  private val imageTabMap: mutable.Map[ImageContent, StackPane] = mutable.Map.empty

  def onImageGridEvent(event: ImageGrid.Event): Unit =
    event match
      case ImageGrid.Event.ImageAdded(image) =>
        val tab = ImageTabPane(image, requestImageRemoval, imagePool)
        children.add(tab.delegate)
        imageTabMap(image) = tab
      case ImageGrid.Event.ImageRemoved(image) =>
        imageTabMap.remove(image) match
          case Some(pane) => children.remove(pane.delegate)
          case None       =>

object ImageTabs:
  def fromImagePool(
      imageGrid: ImageGrid,
      imagePool: ImagePool,
      requestImageRemoval: ImageContent => Unit
  ): TilePane =
    val tilePane = new ImageTabs(imagePool, requestImageRemoval)
    imageGrid.trackChanges(tilePane.onImageGridEvent _)
    tilePane
