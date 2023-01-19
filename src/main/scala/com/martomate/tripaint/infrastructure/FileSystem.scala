package com.martomate.tripaint.infrastructure

import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.util.{EventDispatcher, Tracker}

import java.awt.image.BufferedImage
import java.io.{File, IOException}
import scala.util.Try

class FileSystem private (imageIO: ImageIOWrapper) {
  import FileSystem.Event.*

  private val dispatcher = new EventDispatcher[FileSystem.Event]

  /** @param tracker the tracker to notify when an event occurs */
  def trackChanges(tracker: Tracker[FileSystem.Event]): Unit = dispatcher.track(tracker)

  /** @return Some(image) if it exists, None otherwise */
  def readImage(file: File): Option[RegularImage] = {
    Try(imageIO.read(file)).toOption.map(RegularImage.fromBufferedImage)
  }

  /** @return true if the format is supported and the image was written successfully */
  def writeImage(image: RegularImage, file: File): Boolean = {
    val success = imageIO.write(image.toBufferedImage, getExtension(file).toUpperCase, file)
    if success then dispatcher.notify(ImageWritten(image, file))
    success
  }

  private def getExtension(file: File): String =
    file.getName.substring(file.getName.lastIndexOf('.') + 1)
}

object FileSystem {
  class NullArgs(
      val initialImages: Map[File, RegularImage] = Map.empty,
      val supportedImageFormats: Set[String] = Set("png", "jpg")
  )

  def create(): FileSystem = new FileSystem(new RealImageIO)
  def createNull(args: NullArgs = new NullArgs()): FileSystem = {
    val allSupportedFormats =
      args.supportedImageFormats | args.supportedImageFormats.map(_.toUpperCase)
    val imageIO = new NullImageIO(args.initialImages, allSupportedFormats)
    new FileSystem(imageIO)
  }

  enum Event:
    case ImageWritten(image: RegularImage, file: File)
}

private sealed trait ImageIOWrapper {
  def read(file: File): BufferedImage
  def write(image: BufferedImage, formatName: String, file: File): Boolean
}

private class RealImageIO extends ImageIOWrapper {
  import javax.imageio.ImageIO

  override def read(file: File): BufferedImage = ImageIO.read(file)

  override def write(image: BufferedImage, formatName: String, file: File): Boolean =
    ImageIO.write(image, formatName, file)
}

private class NullImageIO(
    initialImages: Map[File, RegularImage],
    supportedFileFormats: Set[String]
) extends ImageIOWrapper {
  private var images: Map[File, RegularImage] = initialImages.view.mapValues(deepCopy).toMap

  override def read(file: File): BufferedImage = images.get(file) match {
    case Some(image) => image.toBufferedImage
    case None        => throw new IOException("Can't read input file!")
  }

  override def write(image: BufferedImage, formatName: String, file: File): Boolean = {
    if (!supportedFileFormats.contains(formatName.toLowerCase)) {
      return false
    }
    images += file -> RegularImage.fromBufferedImage(image)
    true
  }

  private def deepCopy(bi: RegularImage): RegularImage =
    RegularImage.fromBufferedImage(bi.toBufferedImage)
}
