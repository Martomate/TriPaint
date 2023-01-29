package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.coords.{StorageCoords, TriImageCoords}
import com.martomate.tripaint.model.grid.ImageGrid
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.util.Listenable
import com.martomate.tripaint.view.image.TriImageForPreview
import scalafx.beans.property.ObjectProperty
import scalafx.scene.SnapshotParameters
import scalafx.scene.control.{ScrollPane, Tooltip}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color

object ImagePreviewList:
  def fromImageContent(
      images: Seq[ImageContent],
      previewSize: Int,
      locationOfImage: ImageStorage => Option[SaveLocation]
  ): (ScrollPane, (ImageGrid => Unit) => Unit) =
    val imageSize = if images.nonEmpty then images.head.storage.imageSize else 8

    val scrollPane = new ScrollPane()
    scrollPane.maxWidth = previewSize * 5
    scrollPane.content = new HBox(children = makeContent(_ => ()): _*)
    scrollPane.minViewportHeight = previewSize * Math.sqrt(3) / 2

    def makeContent(effect: ImageGrid => Unit): Seq[ImageView] =
      val previewImages = for im <- images yield cloneImageContent(im)
      val previewImageGrid = new ImageGrid(imageSize)
      for im <- previewImages do previewImageGrid.set(im)
      effect.apply(previewImageGrid)
      for im <- previewImages
      yield ImagePreview.fromImageContent(im, previewSize, locationOfImage)

    (scrollPane, effect => scrollPane.content = new HBox(children = makeContent(effect): _*))

  private def cloneImageContent(content: ImageContent): ImageContent =
    val format = new SimpleStorageFormat
    new ImageContent(
      content.coords,
      ImageStorage
        .fromRegularImage(
          content.storage.toRegularImage(format),
          StorageCoords(0, 0),
          format,
          content.storage.imageSize
        )
        .get
    )

object ImagePreview:
  def fromImageContent(
      content: ImageContent,
      previewSize: Int,
      locationOfImage: ImageStorage => Option[SaveLocation]
  ): ImageView =
    val preview = new TriImageForPreview(content, previewSize)
    val tooltip = TriImageTooltip.fromImagePool(content, locationOfImage)

    val snapshotParams = new SnapshotParameters
    snapshotParams.fill = Color.Transparent

    val view = new ImageView
    view.image = preview.toImage(snapshotParams)
    Tooltip.install(view, tooltip)
    view
