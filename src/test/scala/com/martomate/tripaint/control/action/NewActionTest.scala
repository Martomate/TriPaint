package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.view.TriPaintView
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NewActionTest extends AnyFlatSpec with Matchers {
  "NewAction" should "add a new image to the grid" in {
    val model = TriPaintModel.createNull()
    model.imageGrid.setImageSizeIfEmpty(8)

    val imageSize = model.imageGrid.imageSize
    val backgroundColor = Color.Cyan

    new NewAction(model, backgroundColor, () => Some((3, 4))).perform()

    val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

    val actualImage = model.imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    actualImage shouldBe expectedImage
  }

  it should "do nothing if no image location is provided" in {
    val model = TriPaintModel.createNull()
    model.imageGrid.setImageSizeIfEmpty(8)

    new NewAction(model, null, () => None).perform()

    val actualImage = model.imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    actualImage shouldBe null
  }

  // TODO: Is this really how it should work?
  it should "replace any existing image at the location" in {
    val model = TriPaintModel.createNull()
    model.imageGrid.setImageSizeIfEmpty(8)

    val imageSize = model.imageGrid.imageSize
    val backgroundColor = Color.Cyan

    new NewAction(model, backgroundColor, () => Some((3, 4))).perform()
    new NewAction(model, backgroundColor, () => Some((3, 4))).perform()

    val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

    val actualImage = model.imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    actualImage shouldBe expectedImage
  }
}
