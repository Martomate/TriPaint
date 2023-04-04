package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.view.TriPaintView
import munit.FunSuite

class NewActionTest extends FunSuite {
  test("NewAction should add a new image to the grid") {
    val model = TriPaintModel.createNull()
    model.imageGrid.setImageSizeIfEmpty(8)

    val imageSize = model.imageGrid.imageSize
    val backgroundColor = Color.Cyan

    new NewAction(model, backgroundColor, () => Some((3, 4))).perform()

    val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

    val actualImage = model
      .imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    assertEquals(actualImage, expectedImage)
  }

  test("NewAction should do nothing if no image location is provided") {
    val model = TriPaintModel.createNull()
    model.imageGrid.setImageSizeIfEmpty(8)

    new NewAction(model, null, () => None).perform()

    val actualImage = model
      .imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    assertEquals(actualImage, null)
  }

  // TODO: Is this really how it should work?
  test("NewAction should replace any existing image at the location") {
    val model = TriPaintModel.createNull()
    model.imageGrid.setImageSizeIfEmpty(8)

    val imageSize = model.imageGrid.imageSize
    val backgroundColor = Color.Cyan

    new NewAction(model, backgroundColor, () => Some((3, 4))).perform()
    new NewAction(model, backgroundColor, () => Some((3, 4))).perform()

    val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

    val actualImage = model
      .imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    assertEquals(actualImage, expectedImage)
  }
}
