package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.coords.{PixelCoords, TriImageCoords}
import com.martomate.tripaint.model.grid.{ImageGrid, ImageGridColorLookup, ImageGridSearcher}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.EditMode
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.paint
import javafx.scene.shape.Rectangle
import scalafx.beans.property.ObjectProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

import scala.collection.mutable

class ImageGridPane(imageGrid: ImageGrid) extends Pane:
  private var xScroll: Double = 0
  private var yScroll: Double = 0
  private var zoom: Double = 1d

  private object drag:
    var x: Double = -1
    var y: Double = -1

  object colors:
    val primaryColor: ObjectProperty[paint.Color] = ObjectProperty(Color.Black)
    def primaryColor_=(col: Color): Unit = primaryColor.value = col

    val secondaryColor: ObjectProperty[paint.Color] = ObjectProperty(Color.White)
    def secondaryColor_=(col: Color): Unit = secondaryColor.value = col

  private val gridSearcher: ImageGridSearcher = new ImageGridSearcher(
    new ImageGridColorLookup(imageGrid)
  )

  private val imageMap: mutable.Map[TriImageCoords, TriImage] = mutable.Map.empty

  imageGrid.trackChanges(this.onImageGridEvent _)

  private def triImages: Seq[TriImage] = for im <- imageGrid.images yield imageMap(im.coords)

  private def imageAt(x: Double, y: Double): Option[TriImage] =
    triImages.find(_.coordsAt(x, y) != null)

  onMouseDragged = e =>
    if !e.isConsumed then
      val xDiff = e.getX - drag.x
      val yDiff = e.getY - drag.y

      drag.x = e.getX
      drag.y = e.getY

      EditMode.currentMode match
        case EditMode.Organize => // TODO: implement scale and rotation if (x, y) is close enough to a corner
          setScroll(xScroll + xDiff, yScroll + yDiff)
        case _ =>
          val dist = Math.hypot(xDiff, yDiff) / zoom

          val steps = 4 * dist.toInt + 1
          for i <- 1 to steps do
            val xx = e.getSceneX + xDiff / steps * (i - steps)
            val yy = e.getSceneY + yDiff / steps * (i - steps)

            for
              image <- imageAt(xx, yy)
              if image.content.editable
            do
              val internalCoords = image.coordsAt(xx, yy)
              val coords = PixelCoords(image.content.coords, internalCoords)
              mousePressedAt(coords, e, dragged = true)

  onMousePressed = e =>
    if !e.isConsumed then
      drag.x = e.getX
      drag.y = e.getY

      for
        image <- imageAt(e.getSceneX, e.getSceneY)
        if image.content.editable
      do
        val internalCoords = image.coordsAt(e.getSceneX, e.getSceneY)
        mousePressedAt(PixelCoords(internalCoords, image.content.coords), e, dragged = false)

  onMouseReleased = e =>
    if !e.isConsumed
    then for im <- triImages.reverse do im.onStoppedDrawing()

  onScroll = e =>
    val (dx, dy) = (e.getDeltaX, e.getDeltaY)

    if e.isControlDown then
      val factor = Math.min(Math.max(Math.exp(e.getDeltaY * 0.01), 1.0 / 32 / zoom), 32 / zoom)
      zoom *= factor
      setScroll(xScroll * factor, yScroll * factor)
    else setScroll(xScroll + dx, yScroll + dy)

    if e.isControlDown then for im <- triImages.reverse do im.onZoom(zoom)

  private def setScroll(sx: Double, sy: Double): Unit =
    xScroll = sx
    yScroll = sy
    relocateChildren()

  private def mousePressedAt(coords: PixelCoords, e: MouseEvent, dragged: Boolean): Unit =
    val colorToUse = e.getButton match
      case MouseButton.PRIMARY   => Some(colors.primaryColor)
      case MouseButton.SECONDARY => Some(colors.secondaryColor)
      case _                     => None

    for
      image <- imageGrid(coords.image)
      color <- colorToUse
    do
      EditMode.currentMode match
        case EditMode.Draw =>
          imageMap(image.coords).drawAt(coords.pix, new Color(color()))
        case EditMode.Fill =>
          fill(coords, new Color(color()))
        case EditMode.PickColor =>
          color() = image.storage(coords.pix).toFXColor
        case _ =>

  private def fill(coords: PixelCoords, color: Color): Unit =
    for image <- imageGrid(coords.image) do
      val referenceColor = imageMap(image.coords).content.storage(coords.pix)
      val places = gridSearcher
        .search(coords.toGlobal(imageGrid.imageSize), (_, col) => col == referenceColor)
        .map(p => PixelCoords(p, imageGrid.imageSize))

      for
        p <- places
        im <- imageGrid(p.image)
      do imageMap(im.coords).drawAt(p.pix, color)

  this.width.onChange(updateSize())
  this.height.onChange(updateSize())

  private def updateSize(): Unit =
    this.clip() = new Rectangle(0, 0, width(), height())
    relocateChildren()

  private def relocateChildren(): Unit =
    for im <- imageGrid.images do relocateImage(im)

  private def relocateImage(image: ImageContent): Unit =
    imageMap(image.coords).pane.relocate(width() / 2 + xScroll, height() / 2 + yScroll)

  private def onImageGridEvent(event: ImageGrid.Event): Unit =
    event match
      case ImageGrid.Event.ImageAdded(image) =>
        val triImage = new TriImage(image, zoom)
        children.add(triImage.pane.delegate)
        imageMap(image.coords) = triImage
        relocateImage(image)
      case ImageGrid.Event.ImageRemoved(image) =>
        val index = children.indexOf(imageMap(image.coords).pane.delegate, 0)
        if index != -1 then children.remove(index)
