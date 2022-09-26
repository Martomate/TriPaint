package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.view.image.TriImageForPreview
import scalafx.scene.SnapshotParameters
import scalafx.scene.control.{ScrollPane, Tooltip}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color

class ImagePreviewList(images: Seq[ImageContent], previewSize: Int, imagePool: ImagePool) extends ScrollPane {
  private val snapshotParams = new SnapshotParameters
  snapshotParams.fill = Color.Transparent

  private val imageViews = images.map(im => makeImageView(im, previewSize))

  maxWidth = previewSize * 5
  content = new HBox(children = imageViews: _*)
  minViewportHeight = previewSize * Math.sqrt(3) / 2

  private def makeImageView(content: ImageContent, previewWidth: Double): ImageView = {
    val preview: TriImageForPreview = new TriImageForPreview(content, previewWidth)
    val view = new ImageView
    view.image = preview.toImage(snapshotParams)
    Tooltip.install(view, new TriImageTooltip(content, imagePool))
    view
  }
}
