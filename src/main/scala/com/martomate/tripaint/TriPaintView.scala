package com.martomate.tripaint

import java.io.File

import com.martomate.tripaint.image.graphics.{ImagePane, TriImage}
import com.martomate.tripaint.image.pool.ImageSaveCollisionHandler
import scalafx.scene.paint.Color

trait TriPaintView extends ImageSaveCollisionHandler {
  def imageDisplay: ImagePane

  def askForImageSize(): Option[Int]
  def askSaveBeforeClosing(images: Seq[TriImage]): Option[Boolean]
  def askForWhereToPutImage(): Option[(Int, Int)]
  def askForSaveFile(image: TriImage): Option[File]
  def askForFileToOpen(): Option[File]
  def askForOffset(): Option[(Int, Int)]

  def askForBlurRadius(): Option[Int]
  def askForMotionBlurRadius(): Option[Int]
  def askForRandomNoiseColors(): Option[(Color, Color)]

  def close(): Unit
}
