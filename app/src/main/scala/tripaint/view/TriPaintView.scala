package tripaint.view

import tripaint.Color
import tripaint.coords.StorageCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageSaveCollisionHandler
import tripaint.image.format.StorageFormat

import java.io.File

trait TriPaintView extends ImageSaveCollisionHandler {
  def backgroundColor: Color

  def askForImageSize(): Option[Int]
  def askSaveBeforeClosing(images: Seq[GridCell]): Option[Boolean]
  def askForWhereToPutImage(): Option[(Int, Int)]

  def askForSaveFile(image: GridCell): Option[File]
  def askForFileSaveSettings(file: File, image: GridCell): Option[FileSaveSettings]

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
