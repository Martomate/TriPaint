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

  private val imageViews = images.map(im => makeImageView(new TriImageForPreview(im, previewSize)))

  maxWidth = previewSize * 5
  content = new HBox(children = imageViews: _*)
  minViewportHeight = previewSize * Math.sqrt(3) / 2

  private def makeImageView(preview: TriImageForPreview): ImageView = {
    val view = new ImageView
    view.image = preview.toImage(snapshotParams)
    Tooltip.install(view, new TriImageTooltip(preview.content, imagePool))
    view
  }
}
