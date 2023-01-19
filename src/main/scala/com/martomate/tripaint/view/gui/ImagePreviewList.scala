package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.view.image.TriImageForPreview
import scalafx.scene.SnapshotParameters
import scalafx.scene.control.{ScrollPane, Tooltip}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color

object ImagePreviewList:
  def fromImagePool(images: Seq[ImageContent], previewSize: Int, imagePool: ImagePool): ScrollPane =
    val imageViews = for im <- images yield makeImagePreview(im, previewSize, imagePool)

    val scrollPane = new ScrollPane()
    scrollPane.maxWidth = previewSize * 5
    scrollPane.content = new HBox(children = imageViews: _*)
    scrollPane.minViewportHeight = previewSize * Math.sqrt(3) / 2
    scrollPane

  private def makeImagePreview(
      content: ImageContent,
      previewSize: Int,
      imagePool: ImagePool
  ): ImageView =
    val preview = new TriImageForPreview(content, previewSize)
    val tooltip = TriImageTooltip.fromImagePool(content, imagePool)

    val snapshotParams = new SnapshotParameters
    snapshotParams.fill = Color.Transparent

    val view = new ImageView
    view.image = preview.toImage(snapshotParams)
    Tooltip.install(view, tooltip)
    view
