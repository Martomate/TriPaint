package com.martomate.tripaint.model.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.awt.image.BufferedImage

class RegularImageTest extends AnyFlatSpec with Matchers {
  "ofDim" should "produce an image of correct size" in {
    val image = RegularImage.ofSize(4, 5)
    image.width shouldBe 4
    image.height shouldBe 5
  }

  it should "fill the image with transparent black" in {
    val image = RegularImage.ofSize(1, 2)
    image.getColor(0, 0) shouldBe Color(0, 0, 0, 0)
    image.getColor(0, 1) shouldBe Color(0, 0, 0, 0)
  }

  "fromBufferedImage" should "produce an image of correct size" in {
    val buf = new BufferedImage(2, 5, BufferedImage.TYPE_INT_RGB)
    val image = RegularImage.fromBufferedImage(buf)
    image.width shouldBe 2
    image.height shouldBe 5
  }

  it should "copy the contents" in {
    val buf = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB)
    buf.setRGB(0, 0, 0x123456)
    buf.setRGB(1, 0, 0x345678)

    val image = RegularImage.fromBufferedImage(buf)
    image.getColor(0, 0).toInt shouldBe 0xff123456
    image.getColor(1, 0).toInt shouldBe 0xff345678
  }

  "fill" should "produce an image of correct size" in {
    val image = RegularImage.fill(3, 5, Color.Red)
    image.width shouldBe 3
    image.height shouldBe 5
  }

  it should "fill the image with the color" in {
    val image = RegularImage.fill(3, 5, Color.Red)
    image.getColor(0, 0) shouldBe Color.Red
    image.getColor(0, 4) shouldBe Color.Red
    image.getColor(2, 4) shouldBe Color.Red
    image.getColor(2, 0) shouldBe Color.Red
    image.getColor(1, 1) shouldBe Color.Red
  }

  "getColor" should "return the color at the given location" in {
    val image = RegularImage.ofSize(3, 2)
    image.setColor(2, 1, Color.Red)
    image.getColor(2, 1) shouldBe Color.Red
  }

  it should "return transparent black for unset pixels" in {
    val image = RegularImage.ofSize(3, 2)
    image.getColor(2, 1) shouldBe Color(0, 0, 0, 0)
  }

  it should "fail if the coords are out of bounds" in {
    val image = RegularImage.ofSize(3, 2)

    assertThrows[IllegalArgumentException](image.getColor(-1, 0))
    assertThrows[IllegalArgumentException](image.getColor(0, -1))
    assertThrows[IllegalArgumentException](image.getColor(3, 0))
    assertThrows[IllegalArgumentException](image.getColor(0, 2))
  }

  "setColor" should "fail if the coords are out of bounds" in {
    val image = RegularImage.ofSize(3, 2)

    assertThrows[IllegalArgumentException](image.setColor(-1, 0, Color.Red))
    assertThrows[IllegalArgumentException](image.setColor(0, -1, Color.Red))
    assertThrows[IllegalArgumentException](image.setColor(3, 0, Color.Red))
    assertThrows[IllegalArgumentException](image.setColor(0, 2, Color.Red))
  }

  it should "handle transparency" in {
    val image = RegularImage.ofSize(3, 2)

    image.setColor(1, 0, Color.fromInt(0x12345678))
    image.getColor(1, 0).toInt shouldBe 0x12345678
  }

  "pasteImage" should "overwrite part of dest image with contents of src image" in {
    val dest = RegularImage.ofSize(5, 6)

    val src = RegularImage.ofSize(3, 2)
    src.setColor(0, 0, Color.Red)
    src.setColor(2, 0, Color.Yellow)
    src.setColor(2, 1, Color.Blue)
    src.setColor(0, 1, Color.Cyan)

    dest.pasteImage(StorageCoords(0, 0), src)

    dest.getColor(0, 0) shouldBe Color.Red
    dest.getColor(2, 0) shouldBe Color.Yellow
    dest.getColor(2, 1) shouldBe Color.Blue
    dest.getColor(0, 1) shouldBe Color.Cyan
  }

  it should "keep the content of the dest image outside of the paste area" in {
    val dest = RegularImage.ofSize(5, 6)
    dest.setColor(3, 1, Color.Green)
    dest.setColor(1, 2, Color.Yellow)
    dest.setColor(3, 2, Color.Blue)

    val src = RegularImage.ofSize(3, 2)
    src.setColor(0, 0, Color.Red)
    src.setColor(2, 0, Color.Red)
    src.setColor(2, 1, Color.Red)
    src.setColor(0, 1, Color.Red)

    dest.pasteImage(StorageCoords(0, 0), src)

    dest.getColor(3, 1) shouldBe Color.Green
    dest.getColor(1, 2) shouldBe Color.Yellow
    dest.getColor(3, 2) shouldBe Color.Blue
  }

  it should "paste with an offset" in {
    val dest = RegularImage.ofSize(5, 6)
    dest.setColor(0, 0, Color.Blue)

    val src = RegularImage.ofSize(3, 2)
    src.setColor(0, 0, Color.Red)

    dest.pasteImage(StorageCoords(1, 2), src)

    dest.getColor(0, 0) shouldBe Color.Blue
    dest.getColor(1, 2) shouldBe Color.Red
  }

  it should "work against the border" in {
    val dest = RegularImage.ofSize(5, 6)
    dest.setColor(0, 0, Color.Blue)

    val src = RegularImage.ofSize(3, 2)
    src.setColor(0, 0, Color.Red)

    dest.pasteImage(StorageCoords(2, 4), src)

    dest.getColor(0, 0) shouldBe Color.Blue
    dest.getColor(2, 4) shouldBe Color.Red
  }

  it should "fail if destination image has too small width" in {
    val dest = RegularImage.ofSize(5, 6)
    val src = RegularImage.ofSize(3, 2)
    assertThrows[IllegalArgumentException] {
      dest.pasteImage(StorageCoords(3, 0), src)
    }
  }

  it should "fail if destination image has too small height" in {
    val dest = RegularImage.ofSize(5, 6)
    val src = RegularImage.ofSize(3, 2)
    assertThrows[IllegalArgumentException] {
      dest.pasteImage(StorageCoords(0, 5), src)
    }
  }

  "toBufferedImage" should "produce an image with the same size" in {
    val image = RegularImage.ofSize(3, 2)
    val buf = image.toBufferedImage
    buf.getWidth() shouldBe 3
    buf.getHeight() shouldBe 2
  }

  it should "be correct for simple colors" in {
    val image = RegularImage.ofSize(3, 2)
    image.setColor(0, 0, Color.Red)
    image.setColor(0, 1, Color.Green)
    image.setColor(1, 0, Color.Blue)
    image.setColor(1, 1, Color.Magenta)
    image.setColor(2, 0, Color.Yellow)
    image.setColor(2, 1, Color.Blue)

    val buf = image.toBufferedImage
    Color.fromInt(buf.getRGB(0, 0)) shouldBe Color.Red
    Color.fromInt(buf.getRGB(0, 1)) shouldBe Color.Green
    Color.fromInt(buf.getRGB(1, 0)) shouldBe Color.Blue
    Color.fromInt(buf.getRGB(1, 1)) shouldBe Color.Magenta
    Color.fromInt(buf.getRGB(2, 0)) shouldBe Color.Yellow
    Color.fromInt(buf.getRGB(2, 1)) shouldBe Color.Blue
  }

  it should "ignore transparency" in {
    val image = RegularImage.ofSize(3, 2)
    image.setColor(0, 0, Color.fromInt(0x00123456))
    image.setColor(0, 1, Color.fromInt(0x45123456))

    image.toBufferedImage.getRGB(0, 0) shouldBe 0xff123456
    image.toBufferedImage.getRGB(0, 1) shouldBe 0xff123456
  }

  it should "produce black for unset pixels" in {
    val image = RegularImage.ofSize(3, 2)
    image.toBufferedImage.getRGB(0, 0) shouldBe 0xff000000
  }

  "equals" should "be false if sizes are different" in {
    val image1 = RegularImage.ofSize(3, 2)
    val image2 = RegularImage.ofSize(3, 3)
    image1 == image2 shouldBe false
  }

  it should "be true for new images of same size" in {
    val image1 = RegularImage.ofSize(3, 2)
    val image2 = RegularImage.ofSize(3, 2)
    image1 == image2 shouldBe true
  }

  it should "be false for different images of same size" in {
    val image1 = RegularImage.ofSize(3, 2)
    val image2 = RegularImage.ofSize(3, 2)
    image2.setColor(1, 0, Color.Red)
    image1 == image2 shouldBe false
  }

  it should "check the entire image" in {
    val image1 = RegularImage.ofSize(3, 2)
    val image2 = RegularImage.ofSize(3, 2)
    image2.setColor(2, 1, Color.Red)
    image1 == image2 shouldBe false
  }
}
