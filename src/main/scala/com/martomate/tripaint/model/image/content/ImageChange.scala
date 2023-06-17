package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.image.ImageStorage
import com.martomate.tripaint.model.undo.Change

class ImageChange(val description: String, image: ImageStorage, pixelsChanged: Seq[PixelChange])
    extends Change {
  def redo(): Boolean = {
    for (ch <- pixelsChanged) image.setColor(ch.coords, ch.after)
    true
  }

  def undo(): Boolean = {
    for (ch <- pixelsChanged) image.setColor(ch.coords, ch.before)
    true
  }
}
