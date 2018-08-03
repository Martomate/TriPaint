package com.martomate.tripaint.gui

import com.martomate.tripaint.image.storage.ImagePool
import com.martomate.tripaint.image.{TriImage, TriImagePreview}
import scalafx.scene.SnapshotParameters
import scalafx.scene.control.{ScrollPane, Tooltip}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color

class ImagePreviewList(images: Seq[TriImage], previewSize: Int, imagePool: ImagePool) extends ScrollPane {
  private val snapshotParams = new SnapshotParameters
  snapshotParams.fill = Color.Transparent

  private val imageViews = images.map(im => makeImageView(new TriImagePreview(previewSize, im)))

  maxWidth = previewSize * 5
  content = new HBox(children = imageViews: _*)
  minViewportHeight = previewSize * Math.sqrt(3) / 2

  private def makeImageView(preview: TriImagePreview): ImageView = {
    val view = new ImageView
    view.image = preview.toImage(snapshotParams)
    Tooltip.install(view, new TriImageTooltip(preview.image.content, imagePool))
    view
  }
}
