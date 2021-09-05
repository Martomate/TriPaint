package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.coords.{PixelCoords, TriImageCoords}
import com.martomate.tripaint.model.grid.{ImageGrid, ImageGridColorLookup, ImageGridListener, ImageGridSearcher}
import com.martomate.tripaint.view.EditMode
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.paint
import javafx.scene.shape.Rectangle
import scalafx.beans.property.ObjectProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

import scala.collection.mutable

class ImagePane(imageGrid: ImageGrid) extends Pane with ImageGridView with ImageGridListener {
  private var _zoom = 1d
  def zoom: Double = _zoom

  private var _xScroll: Double = 0
  def xScroll: Double = _xScroll

  private var _yScroll: Double = 0
  def yScroll: Double = _yScroll

  private val gridSearcher: ImageGridSearcher = new ImageGridSearcher(new ImageGridColorLookup(imageGrid))

  private val imageMap: mutable.Map[TriImageCoords, TriImage] = mutable.Map.empty

  imageGrid.addListener(this)

  private def images: mutable.Seq[ImageContent] = imageGrid.images
  private def triImages: mutable.Seq[TriImage] = images.map(im => imageMap(im.coords))
  def imageSize: Int = imageGrid.imageSize

  def sideLength: Double = (imageSize * 2 + 1) * zoom

  private object drag {
    var x: Double = -1
    var y: Double = -1
  }

  private def imageAt(x: Double, y: Double): Option[TriImage] = {
    //    val xx = (x - width() / 2 - xScroll) / sideLength
    //    val yy = (y - height() / 2 - yScroll) / sideLength

    triImages find { im =>
      im.coordsAt(x, y) != null
    }
  }

  onMouseDragged = e => {
    if (!e.isConsumed) {
      val xPos = e.getX
      val yPos = e.getY

      EditMode.currentMode match {
        case EditMode.Organize => // TODO: implement scale and rotation if (x, y) is close enough to a corner
          setScroll(xScroll + xPos - drag.x, yScroll + yPos - drag.y)
        case _ =>
          if (true) {
            val xDiff = xPos - drag.x
            val yDiff = yPos - drag.y
            val dist = Math.hypot(xDiff, yDiff) / zoom

            val steps = 4 * dist.toInt + 1
            //		println(s"$steps\t$dist")
            for (i <- 1 to steps) {
              val xx = e.getSceneX + xDiff / steps * (i - steps)
              val yy = e.getSceneY + yDiff / steps * (i - steps)
              imageAt(xx, yy).filter(_.content.editable) match {
                case Some(image) =>
                  val internalCoords = image.coordsAt(xx, yy)
                  mousePressedAt(PixelCoords(internalCoords, image.content.coords), e, dragged = true)
                case _ =>
              }
            }
          }
      }
      drag.x = xPos
      drag.y = yPos
    }
  }
  onMousePressed = e => {
    if (!e.isConsumed) {
      drag.x = e.getX
      drag.y = e.getY
      imageAt(e.getSceneX, e.getSceneY).filter(_.content.editable) foreach { image =>
        val internalCoords = image.coordsAt(e.getSceneX, e.getSceneY)
        mousePressedAt(PixelCoords(internalCoords, image.content.coords), e, dragged = false)
      }
    }
  }
  onMouseReleased = e => triImages.reverse.foreach(_.onMouseReleased(e))
  onScroll = e => {
    val (dx, dy) = (e.getDeltaX, e.getDeltaY)

    if (e.isControlDown) {
      val factor = Math.min(Math.max(Math.exp(e.getDeltaY * 0.01), 1.0 / 32 / _zoom), 32 / _zoom)
      _zoom *= factor
      setScroll(xScroll * factor, yScroll * factor)
    } else {
      setScroll(xScroll + dx, yScroll + dy)
    }
    triImages.reverse.foreach(_.onScroll(e))
  }

  private def setScroll(sx: Double, sy: Double): Unit = {
    _xScroll = sx
    _yScroll = sy
    relocateChildren()
  }

  private def mousePressedAt(coords: PixelCoords, e: MouseEvent, dragged: Boolean): Unit = {
    for {
      image <- imageGrid(coords.image)
      color <- primaryOrSecondaryColor
    } EditMode.currentMode match {
      case EditMode.Draw =>
          imageMap(image.coords).drawAt(coords.pix, new Color(color()))
      case EditMode.Fill =>
          fill(coords, new Color(color()))
      case EditMode.PickColor =>
          color() = image.storage(coords.pix)
      case _ =>
    }

    def primaryOrSecondaryColor: Option[ObjectProperty[paint.Color]] = e.getButton match {
      case MouseButton.PRIMARY => Some(colors.primaryColor)
      case MouseButton.SECONDARY => Some(colors.secondaryColor)
      case _ => None
    }
  }

  def fill(coords: PixelCoords, color: Color): Unit = {
    for (image <- imageGrid(coords.image)) {
      val referenceColor = imageMap(image.coords).content.storage(coords.pix)
      val places = gridSearcher.search(coords.toGlobal(imageSize), (_, col) => col == referenceColor).map(p => PixelCoords(p, imageSize))

      for {
        p <- places
        im <- imageGrid(p.image)
      } imageMap(im.coords).drawAt(p.pix, color)
    }
  }

  object colors {
    val primaryColor: ObjectProperty[paint.Color] = ObjectProperty(Color.Black)
    def primaryColor_=(col: Color): Unit = primaryColor.value = col

    val secondaryColor: ObjectProperty[paint.Color] = ObjectProperty(Color.White)
    def secondaryColor_=(col: Color): Unit = secondaryColor.value = col
  }

  def undo: Boolean = {
    triImages.foreach(_.undo())
    true
  }
  def redo: Boolean = {
    triImages.foreach(_.redo())
    true
  }

  this.width  onChange updateSize
  this.height onChange updateSize

  private def updateSize(): Unit = {
    this.clip() = new Rectangle(0, 0, width(), height())
    relocateChildren()
  }

  private def relocateChildren(): Unit = {
    images.foreach(relocateImage)
  }

  private def relocateImage(image: ImageContent): Unit = {
    imageMap(image.coords).relocate(width() / 2 + xScroll, height() / 2 + yScroll)
  }

  override def onAddImage(image: ImageContent): Unit = {
    val triImage = TriImageImpl(image, this)
    children add triImage.pane
    imageMap(image.coords) = triImage
    relocateImage(image)
  }

  override def onRemoveImage(image: ImageContent): Unit = {
    val index = children indexOf imageMap(image.coords).pane.delegate

    if (index != -1) {
      children.remove(index)
    }
  }
}
