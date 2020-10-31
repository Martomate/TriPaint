package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.undo.Change

class ImageChange(val description: String, val image: ImageContent, pixelsChanged: Seq[PixelChange]) extends Change {
  def redo(): Boolean = {
    val draw = image.storage
    for (ch <- pixelsChanged) draw(ch.coords) = ch.after
    image.changeTracker.tellListenersAboutBigChange()
    true
  }

  def undo(): Boolean = {
    val draw = image.storage
    for (ch <- pixelsChanged) draw(ch.coords) = ch.before
    image.changeTracker.tellListenersAboutBigChange()
    true
  }
}
