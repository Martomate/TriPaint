package tripaint

import scala.collection.mutable.ArrayBuffer
import scalafx.scene.layout.Pane
import scalafx.scene.control.ToggleButton
import scalafx.scene.control.Tooltip
import javafx.event.EventHandler
import javafx.event.ActionEvent
import scalafx.beans.property.ObjectProperty
import javafx.scene.shape.Rectangle
import tripaint.image.TriImage
import scalafx.scene.paint.Color

class ImagePane extends Pane {
  private val images = ArrayBuffer.empty[TriImage]
  
  private val (_primaryColor, _secondaryColor) = (ObjectProperty(Color.Black), ObjectProperty(Color.White))
  private var _globalZoom = 1d
  def globalZoom = _globalZoom
  
  onMouseDragged = e => images.reverse.foreach(_.onMouseDragged(e))
	onMousePressed = e => images.reverse.foreach(_.onMousePressed(e))
	onMouseReleased = e => images.reverse.foreach(_.onMouseReleased(e))
	onScroll = e => {
	  if (e.isControlDown()) _globalZoom *= Math.exp(e.getDeltaY * 0.01)
	  images.reverse.foreach(_.onScroll(e))
	}
	
	def primaryColor = _primaryColor
	def primaryColor_=(col: Color) = primaryColor.value = col
	def secondaryColor = _secondaryColor
	def secondaryColor_=(col: Color) = secondaryColor.value = col
	def getImages = images.toVector
	def getSelectedImages = getImages.filter(_.isSelected)
  
  def addImage(image: TriImage): Unit = images append image
  
  def removeImage(image: TriImage): Unit = {
    val index = images indexOf image
    
    if (index != -1) removeImage(index)
  }
  
  def removeImage(index: Int): Unit = {
	  children.remove(index)
    images remove index
    if (getSelectedImages.size == 0 && images.size != 0) selectImage(images(images.size-1), false)
  }
  
  def selectImage(image: TriImage, replace: Boolean): Unit = {
    if (replace) images.foreach(im => im.selected() = im eq image)
    else image.selected() = !image.selected()
  }
  
  def undo: Boolean = {
    var result = true
    for (im <- getSelectedImages) {
      if (!im.undo) result = false
    }
    result
  }
  
  def redo: Boolean = {
    var result = true
    for (im <- getSelectedImages) {
      if (!im.redo) result = false
    }
    result
  }
  
  this.width onChange updateSize
  this.height onChange updateSize
  def updateSize: Unit = {
    this.clip() = new Rectangle(0, 0, width(), height())
		images.foreach(x => x.updateLocation)
	}
}