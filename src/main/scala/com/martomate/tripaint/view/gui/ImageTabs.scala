package com.martomate.tripaint.view.gui

import com.martomate.tripaint.control.TriPaintController
import com.martomate.tripaint.view.image.grid.ImageGridListener
import com.martomate.tripaint.view.image.TriImage
import scalafx.scene.layout.TilePane

import scala.collection.mutable

class ImageTabs(controls: TriPaintController) extends TilePane with ImageGridListener {
  maxWidth = TriImage.previewSize
  controls.imageGrid.addListener(this)

  private val imageTabMap: mutable.Map[TriImage, ImageTabPane] = mutable.Map.empty

  def onAddImage(image: TriImage): Unit = {
    val tab = ImageTabPane(image, controls)
    children.add(tab.delegate)
    imageTabMap(image) = tab
  }

  def onRemoveImage(image: TriImage): Unit = {
    imageTabMap.remove(image).foreach(pane =>
      children.remove(pane.delegate)
    )
  }
}
