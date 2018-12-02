package com.martomate.tripaint.image.grid

import com.martomate.tripaint.image.content.ImageContent
import com.martomate.tripaint.image.coords.TriImageCoords
import com.martomate.tripaint.image.graphics.TriImage
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

abstract class ImageGridTest extends FlatSpec with Matchers with MockFactory {
  def make: ImageGrid

  def makeImage(x: Int, y: Int): TriImage = {
    val image = stub[TriImage]
    val content = new ImageContent(TriImageCoords(x, y), null)
    image.content _ when () returns content
    image
  }

  "apply" should "return None if there is no image there" in {
    val f = make
    val image = makeImage(1, 0)

    f(TriImageCoords(0, 0)) shouldBe None
    f(TriImageCoords(1, 0)) = image
    f(TriImageCoords(0, 0)) shouldBe None
  }

  it should "return the image at the given location" in {
    val f = make
    val image = makeImage(1, 0)

    f(TriImageCoords(1, 0)) shouldBe None
    f(TriImageCoords(1, 0)) = image
    f(TriImageCoords(1, 0)) shouldBe Some(image)
    f(TriImageCoords(0, 1)) shouldBe None
    f(TriImageCoords(1, 0)) shouldBe Some(image)
  }

  "update" should "add the image if it doesn't already exist" in {
    val f = make
    val image = makeImage(1, 0)

    f(TriImageCoords(1, 0)) = image
    f(TriImageCoords(1, 0)) shouldBe Some(image)
  }

  it should "replace the image if there is already one at that location" in {
    val f = make
    val image = makeImage(1, 0)
    val image2 = makeImage(1, 0)

    f(TriImageCoords(1, 0)) = image
    f(TriImageCoords(1, 0)) = image2
    f(TriImageCoords(1, 0)) shouldBe Some(image2)
  }

  it should "notify listeners about image addition, and image removal if there was one" in {
    val f = make
    val image = makeImage(1, 0)
    val image2 = makeImage(1, 0)

    val listener = mock[ImageGridListener]
    f.addListener(listener)

    listener.onAddImage _ expects image
    f(TriImageCoords(1, 0)) = image

    listener.onAddImage _ expects image2
    listener.onRemoveImage _ expects image
    f(TriImageCoords(1, 0)) = image2
  }

  "-=" should "return null if there is no image there" in {
    val f = make
    (f -= TriImageCoords(1, 2)) shouldBe null
  }

  it should "remove the image and return it if it exists" in {
    val f = make
    val image = makeImage(1, 0)

    f(TriImageCoords(1, 0)) = image
    (f -= TriImageCoords(1, 0)) shouldBe image
    f(TriImageCoords(1, 0)) shouldBe None
  }

  it should "notify listeners if there was a removal" in {
    val f = make
    val image = makeImage(1, 0)

    f(TriImageCoords(1, 0)) = image

    val listener = mock[ImageGridListener]
    f.addListener(listener)

    f -= TriImageCoords(0, 0)

    listener.onRemoveImage _ expects image
    f -= TriImageCoords(1, 0)
  }

  "selectedImages" should "return all images that are currently selected" in {
    val f = make
    val image = makeImage(1, 0)
    val image2 = makeImage(2, 0)

    f(TriImageCoords(1, 0)) = image
    f(TriImageCoords(2, 0)) = image2

    f.selectedImages.sortBy(_.##) shouldBe Seq(image, image2).sortBy(_.##)
    image.content.editableProperty() = false
    f.selectedImages.sortBy(_.##) shouldBe Seq(image2).sortBy(_.##)
    image.content.editableProperty() = true
    image2.content.editableProperty() = false
    f.selectedImages.sortBy(_.##) shouldBe Seq(image).sortBy(_.##)
    image2.content.editableProperty() = true
    f.selectedImages.sortBy(_.##) shouldBe Seq(image, image2).sortBy(_.##)
  }
}
