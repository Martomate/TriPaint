package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.grid.ImageGridListener
import com.martomate.tripaint.view.TriPaintViewListener
import com.martomate.tripaint.view.image.TriImage
import scalafx.scene.layout.TilePane

import scala.collection.mutable

class ImageTabs(controls: TriPaintViewListener, model: TriPaintModel) extends TilePane with ImageGridListener {
  maxWidth = TriImage.previewSize
  model.imageGrid.addListener(this)

  private val imageTabMap: mutable.Map[ImageContent, ImageTabPane] = mutable.Map.empty

  def onAddImage(image: ImageContent): Unit = {
    val tab = ImageTabPane(image, controls, model)
    children.add(tab.delegate)
    imageTabMap(image) = tab
  }

  def onRemoveImage(image: ImageContent): Unit = {
    imageTabMap.remove(image).foreach(pane =>
      children.remove(pane.delegate)
    )
  }
}
