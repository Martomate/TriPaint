package com.martomate.tripaint.view.gui

import com.martomate.tripaint.control.TriPaintController
import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.view.image.grid.ImageGridListener
import com.martomate.tripaint.view.image.TriImage
import scalafx.scene.layout.TilePane

import scala.collection.mutable

class ImageTabs(controls: TriPaintController) extends TilePane with ImageGridListener {
  maxWidth = TriImage.previewSize
  controls.model.imageGrid.addListener(this)

  private val imageTabMap: mutable.Map[ImageContent, ImageTabPane] = mutable.Map.empty

  def onAddImage(image: ImageContent): Unit = {
    val tab = ImageTabPane(image, controls)
    children.add(tab.delegate)
    imageTabMap(image) = tab
  }

  def onRemoveImage(image: ImageContent): Unit = {
    imageTabMap.remove(image).foreach(pane =>
      children.remove(pane.delegate)
    )
  }
}
