package com.martomate.tripaint.view

import java.io.File

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.pool.ImageSaveCollisionHandler
import com.martomate.tripaint.util.Listenable
import com.martomate.tripaint.view.image.ImagePane
import scalafx.scene.paint.Color

trait TriPaintView extends ImageSaveCollisionHandler with Listenable[TriPaintViewListener] {
  def imageDisplay: ImagePane

  def askForImageSize(): Option[Int]
  def askSaveBeforeClosing(images: Seq[ImageContent]): Option[Boolean]
  def askForWhereToPutImage(): Option[(Int, Int)]
  def askForSaveFile(image: ImageContent): Option[File]
  def askForFileToOpen(): Option[File]
  def askForOffset(): Option[(Int, Int)]

  def askForBlurRadius(): Option[Int]
  def askForMotionBlurRadius(): Option[Int]
  def askForRandomNoiseColors(): Option[(Color, Color)]

  def close(): Unit
}
