package tripaint.view.gui

import tripaint.coords.StorageCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool.SaveLocation
import tripaint.image.ImageStorage
import tripaint.image.format.SimpleStorageFormat
import tripaint.view.image.TriImageForPreview

import javafx.scene.SnapshotParameters
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.paint.Color

object ImagePreviewList {
  def fromImageContent(
      images: Seq[GridCell],
      previewSize: Int,
      locationOfImage: ImageStorage => Option[SaveLocation]
  ): (ScrollPane, (ImageGrid => Unit) => Unit) = {
    val imageSize = if images.nonEmpty then images.head.storage.imageSize else 8

    def makeContent(effect: ImageGrid => Unit): Seq[ImageView] = {
      val previewImages = images.map(cloneImageContent)

      val previewImageGrid = new ImageGrid(imageSize)
      for im <- previewImages do {
        previewImageGrid.set(im)
      }

      effect.apply(previewImageGrid)

      previewImages.map(ImagePreview.fromImageContent(_, previewSize, locationOfImage))
    }

    val scrollPane = {
      val p = new ScrollPane()
      p.setMaxWidth(previewSize * 5)
      p.setContent(new HBox(makeContent(_ => ())*))
      p.setMinViewportHeight(previewSize * Math.sqrt(3) / 2)
      p
    }

    val updatePreview: (ImageGrid => Unit) => Unit = effect => {
      scrollPane.setContent(new HBox(makeContent(effect)*))
    }

    (scrollPane, updatePreview)
  }

  private def cloneImageContent(content: GridCell): GridCell = {
    val format = SimpleStorageFormat
    val image = content.storage.toRegularImage(format)
    val imageSize = content.storage.imageSize
    val storage = ImageStorage.fromRegularImage(image, StorageCoords(0, 0), format, imageSize).get
    new GridCell(content.coords, storage)
  }
}

object ImagePreview {
  def fromImageContent(
      content: GridCell,
      previewSize: Int,
      locationOfImage: ImageStorage => Option[SaveLocation]
  ): ImageView = {
    val preview = new TriImageForPreview(content, previewSize)
    val tooltip = TriImageTooltip.fromImagePool(content, locationOfImage)

    val snapshotParams = new SnapshotParameters
    snapshotParams.setFill(Color.TRANSPARENT)

    val view = new ImageView
    view.setImage(preview.toImage(snapshotParams))
    Tooltip.install(view, tooltip)
    view
  }
}
