package com.martomate.tripaint.infrastructure

import java.awt.image.BufferedImage
import java.io.{File, IOException}
import scala.util.Try

class FileSystem private (imageIO: ImageIOWrapper) {
  /** @return Some(image) if it exists, None otherwise */
  def readImage(file: File): Option[BufferedImage] = {
    Try(imageIO.read(file)).toOption
  }

  /** @return true if the format is supported and the image was written successfully */
  def writeImage(image: BufferedImage, file: File): Boolean = {
    imageIO.write(image, getExtension(file).toUpperCase, file)
  }

  private def getExtension(file: File): String = file.getName.substring(file.getName.lastIndexOf('.') + 1)
}

object FileSystem {
  def create(): FileSystem = new FileSystem(new RealImageIO)
  def createNull(initialImages: Map[File, BufferedImage] = Map.empty,
                 supportedImageFormats: Set[String] = Set("png", "jpg")): FileSystem = {
    val allSupportedFormats = supportedImageFormats | supportedImageFormats.map(_.toUpperCase)
    val imageIO = new NullImageIO(initialImages, allSupportedFormats)
    new FileSystem(imageIO)
  }
}

trait ImageIOWrapper {
  def read(file: File): BufferedImage
  def write(image: BufferedImage, formatName: String, file: File): Boolean
}

class RealImageIO extends ImageIOWrapper {
  import javax.imageio.ImageIO

  override def read(file: File): BufferedImage = ImageIO.read(file)

  override def write(image: BufferedImage, formatName: String, file: File): Boolean =
    ImageIO.write(image, formatName, file)
}

class NullImageIO(initialImages: Map[File, BufferedImage],
                  supportedFileFormats: Set[String]) extends ImageIOWrapper {
  private var images: Map[File, BufferedImage] = initialImages.view.mapValues(deepCopy).toMap

  override def read(file: File): BufferedImage = images.get(file) match {
    case Some(image) => deepCopy(image)
    case None => throw new IOException("Can't read input file!")
  }

  override def write(image: BufferedImage, formatName: String, file: File): Boolean = {
    if (!supportedFileFormats.contains(formatName.toLowerCase)) {
      return false
    }
    images += file -> deepCopy(image)
    true
  }

  private def deepCopy(bi: BufferedImage): BufferedImage = {
    val cm = bi.getColorModel
    val isAlphaPremultiplied = cm.isAlphaPremultiplied
    val raster = bi.copyData(null)
    new BufferedImage(cm, raster, isAlphaPremultiplied, null)
  }
}
