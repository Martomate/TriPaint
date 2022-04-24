package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.{RegularImage, SaveLocation}
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.view.TriPaintView
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NewActionTest extends AnyFlatSpec with Matchers with MockFactory {
  "NewAction" should "add a new image to the grid" in {
    val model = TriPaintModel.createNull()
    val view = mock[TriPaintView]
    model.imageGrid.setImageSizeIfEmpty(8)

    val imageSize = model.imageGrid.imageSize
    val backgroundColor = Color.Cyan

    (view.askForWhereToPutImage _).expects() returns Some((3, 4))
    (view.backgroundColor _).expects() returns backgroundColor.toFXColor

    NewAction.perform(model, view)

    val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

    val actualImage = model.imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    actualImage shouldBe expectedImage
  }

  it should "do nothing if no image location is provided" in {
    val model = TriPaintModel.createNull()
    val view = mock[TriPaintView]
    model.imageGrid.setImageSizeIfEmpty(8)

    (view.askForWhereToPutImage _).expects() returns None

    NewAction.perform(model, view)

    val actualImage = model.imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    actualImage shouldBe null
  }

  // TODO: Is this really how it should work?
  it should "replace any existing image at the location" in {
    val model = TriPaintModel.createNull()
    val view = mock[TriPaintView]
    model.imageGrid.setImageSizeIfEmpty(8)

    val imageSize = model.imageGrid.imageSize
    val backgroundColor = Color.Cyan

    (view.askForWhereToPutImage _).expects() returns Some((3, 4))
    (view.backgroundColor _).expects() returns backgroundColor.toFXColor

    NewAction.perform(model, view)

    (view.askForWhereToPutImage _).expects() returns Some((3, 4))
    (view.backgroundColor _).expects() returns backgroundColor.toFXColor

    NewAction.perform(model, view)

    val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

    val actualImage = model.imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    actualImage shouldBe expectedImage
  }
}
