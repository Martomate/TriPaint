package com.martomate.tripaint.control.action

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.model.coords.{StorageCoords, TriImageCoords}
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.view.FileOpenSettings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

class OpenActionTest extends AnyFlatSpec with Matchers {
  "OpenAction" should "open an image" in {
    val file = new File("image.png")
    val image = RegularImage.fill(8, 8, Color.Yellow)
    image.setColor(5, 6, Color.Cyan)

    val model =
      TriPaintModel.createNull(new FileSystem.NullArgs(initialImages = Map(file -> image)))
    model.imageGrid.setImageSizeIfEmpty(8)

    new OpenAction(
      model,
      () => Some(file),
      (_, _, _, _) => Some(FileOpenSettings(StorageCoords(0, 0), new SimpleStorageFormat)),
      () => Some((3, 4))
    ).perform()

    val actualImage = model
      .imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    actualImage shouldBe image
  }

  it should "open an image at an offset" in {
    val file = new File("image.png")
    val image = RegularImage.fill(8, 8, Color.Yellow)
    image.setColor(5, 6, Color.Cyan)

    val storedImage = RegularImage.ofSize(9, 10)
    val offset = StorageCoords(1, 2)
    storedImage.pasteImage(offset, image)

    val model = TriPaintModel.createNull(
      new FileSystem.NullArgs(initialImages = Map(file -> storedImage))
    )
    model.imageGrid.setImageSizeIfEmpty(8)

    new OpenAction(
      model,
      () => Some(file),
      (_, _, _, _) => Some(FileOpenSettings(offset, new SimpleStorageFormat)),
      () => Some((3, 4))
    ).perform()

    val actualImage = model
      .imageGrid(TriImageCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(new SimpleStorageFormat))
      .orNull

    actualImage shouldBe image
  }
}
