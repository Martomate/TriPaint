package com.martomate.tripaint.infrastructure

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.awt.image.BufferedImage
import java.io.File

class FileSystemTest extends AnyFlatSpec with Matchers {

  private val tempDir: String = System.getProperty("java.io.tmpdir")

  "readImage" should "return None if the image does not exist" in {
    val fs = FileSystem.create()
    val file = new File(tempDir, "a_non_existent_file_93784.png")
    file.exists() shouldBe false
    val image = fs.readImage(file)
    image shouldBe None
  }

  it should "return the image if it exists"

  "writeImage" should "return false if the format is not supported" in {
    val fs = FileSystem.create()
    val image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
    val file = new File(tempDir, "filename72454.xyz")

    try {
      val success = fs.writeImage(image, file)

      // The 'xyz' extension is not supported, so the write is aborted
      success shouldBe false
      file.exists() shouldBe false
    } finally {
      // Clean up if needed
      file.delete()
    }
  }

  it should "return true if the image was written" in {
    val fs = FileSystem.create()
    val image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
    val file = new File(tempDir, "filename38475.png")
    file.exists() shouldBe false

    try {
      val success = fs.writeImage(image, file)

      // The 'png' extension is supported, so the write can proceed
      success shouldBe true
      file.exists() shouldBe true
    } finally {
      // Clean up after the test
      file.delete()
    }
  }

}
