package com.martomate.tripaint.view

import com.martomate.tripaint.model.image.content.ImageContent

trait TriPaintViewListener {
  def action_new(): Unit
  def action_open(): Unit
  def action_openHexagon(): Unit
  def action_save(): Unit
  def action_saveAs(): Unit
  def action_exit(): Unit

  def action_undo(): Unit
  def action_redo(): Unit

  def action_blur(): Unit
  def action_motionBlur(): Unit
  def action_randomNoise(): Unit
  def action_scramble(): Unit

  /** Returns whether to exit or not */
  def requestExit(): Boolean
  def requestImageRemoval(image: ImageContent): Unit
}
