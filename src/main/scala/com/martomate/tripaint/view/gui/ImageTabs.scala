package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.grid.{ImageGrid, ImageGridListener}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.view.TriPaintViewListener
import com.martomate.tripaint.view.image.TriImageForPreview
import scalafx.scene.layout.{StackPane, TilePane}

import scala.collection.mutable

class ImageTabs(imagePool: ImagePool, requestImageRemoval: ImageContent => Unit)
    extends TilePane
    with ImageGridListener:
  maxWidth = TriImageForPreview.previewSize

  private val imageTabMap: mutable.Map[ImageContent, StackPane] = mutable.Map.empty

  def onAddImage(image: ImageContent): Unit =
    val tab = ImageTabPane(image, requestImageRemoval, imagePool)
    children.add(tab.delegate)
    imageTabMap(image) = tab

  def onRemoveImage(image: ImageContent): Unit =
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
    imageGrid.addListener(tilePane)
    tilePane
