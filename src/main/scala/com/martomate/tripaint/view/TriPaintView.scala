package com.martomate.tripaint.view

import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.ImageSaveCollisionHandler

import java.io.File
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.format.StorageFormat
import scalafx.scene.paint.Color

trait TriPaintView extends ImageSaveCollisionHandler {
  def backgroundColor: Color

  def askForImageSize(): Option[Int]
  def askSaveBeforeClosing(images: Seq[ImageContent]): Option[Boolean]
  def askForWhereToPutImage(): Option[(Int, Int)]

  def askForSaveFile(image: ImageContent): Option[File]
  def askForFileSaveSettings(file: File, image: ImageContent): Option[FileSaveSettings]

  def askForFileToOpen(): Option[File]
  def askForFileOpenSettings(
      file: File,
      imageSize: Int,
      xCount: Int,
      yCount: Int
  ): Option[FileOpenSettings]

  def askForBlurRadius(): Option[Int]
  def askForMotionBlurRadius(): Option[Int]
  def askForRandomNoiseColors(): Option[(Color, Color)]

  def close(): Unit
}

case class FileSaveSettings(offset: StorageCoords, format: StorageFormat)
case class FileOpenSettings(offset: StorageCoords, format: StorageFormat)
