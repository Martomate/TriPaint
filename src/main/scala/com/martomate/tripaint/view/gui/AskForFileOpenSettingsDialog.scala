package com.martomate.tripaint.view.gui

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{StorageCoords, TriImageCoords}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.view.FileOpenSettings
import com.martomate.tripaint.view.gui.DialogUtils.{getValueFromCustomDialog, makeGridPane}
import com.martomate.tripaint.view.image.TriImageForPreview
import scalafx.collections.ObservableBuffer
import scalafx.embed.swing.SwingFXUtils
import scalafx.geometry.Orientation
import scalafx.scene.control.{ButtonType, ChoiceBox, Label, Separator}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane
import scalafx.util.StringConverter

import java.io.{File, FileInputStream}
import scala.util.{Success, Try}

object AskForFileOpenSettingsDialog:
  def askForFileOpenSettings(
      imagePreview: (File, Int, Int, Int),
      formats: Seq[(StorageFormat, String)],
      initiallySelectedFormat: Int,
      fileSystem: FileSystem
  ): Option[FileOpenSettings] = {
    val (previewFile, imageSize, xCount, yCount) = imagePreview
    val (previewWidth, previewHeight) = (xCount * imageSize, yCount * imageSize)

    val xCoordTF = RestrictedTextField.uintTF
    xCoordTF.promptText = "0"

    val yCoordTF = RestrictedTextField.uintTF
    yCoordTF.promptText = "0"

    val formatMap: Map[StorageFormat, String] = Map.from(formats)

    val formatChooser = new ChoiceBox[StorageFormat](ObservableBuffer(formats.map(_._1): _*))
    formatChooser.selectionModel.value.select(initiallySelectedFormat)
    formatChooser.converter = StringConverter.toStringConverter(formatMap(_))

    val resultFromInputs = () => {
      val xt = xCoordTF.text()
      val yt = yCoordTF.text()
      val format = formatChooser.selectionModel.value.getSelectedItem

      for
        xOffset <- Try(if xt != "" then xt.toInt else 0)
        yOffset <- Try(if yt != "" then yt.toInt else 0)
      yield FileOpenSettings(StorageCoords(xOffset, yOffset), format)
    }

    val previewPane = new Pane
    previewPane.setMinSize(previewWidth, previewHeight)
    previewPane.setMaxSize(previewWidth, previewHeight)

    {
      import javafx.scene.layout.*
      import javafx.scene.paint.Color
      val stroke = new BorderStroke(
        Color.RED,
        BorderStrokeStyle.SOLID,
        CornerRadii.EMPTY,
        BorderWidths.DEFAULT
      )
      previewPane.delegate.setBorder(new Border(stroke))
    }

    val underlyingImage = fileSystem.readImage(previewFile).get

    val wholeImage = new ImageView(SwingFXUtils.toFXImage(underlyingImage.toBufferedImage, null))

    val previewStack = new Pane
    previewStack.delegate.getChildren.addAll(wholeImage, previewPane)

    val blankImage = ImageStorage.fromBGColor(Color.White, imageSize)
    val triPreviewImages =
      Seq.tabulate(xCount)(_ => new ImageContent(TriImageCoords(0, 0), blankImage))

    val (triPreviewPane, updateTriPreview) = ImagePreviewList.fromImageContent(
      triPreviewImages,
      TriImageForPreview.previewSize,
      _ => None
    )

    def updatePreviewAction(): Unit =
      resultFromInputs() match
        case Success(FileOpenSettings(StorageCoords(sx, sy), format)) =>
          previewPane.setLayoutX(sx)
          previewPane.setLayoutY(sy)

          for x <- 0 until xCount do
            ImageStorage.fromRegularImage(
              underlyingImage,
              StorageCoords(sx + x * imageSize, sy),
              format,
              imageSize
            ) match
              case Success(imageStorage) =>
                triPreviewImages(x).replaceImage(imageStorage)
              case _ =>
                triPreviewImages(x).replaceImage(blankImage)

          updateTriPreview(_ => ())
        case _ =>

    updatePreviewAction()

    xCoordTF.text.onChange(updatePreviewAction())
    yCoordTF.text.onChange(updatePreviewAction())
    formatChooser.onAction = _ => updatePreviewAction()

    getValueFromCustomDialog[FileOpenSettings](
      title = "Open image",
      headerText = "Which part of the image should be opened? Please enter the top left corner:",
      content = Seq(
        makeGridPane(
          Seq(
            Seq(new Label("X coordinate:"), xCoordTF),
            Seq(new Label("Y coordinate:"), yCoordTF),
            Seq(new Label("Format:"), formatChooser)
          )
        ),
        Separator(Orientation.Horizontal),
        previewStack
      ),
      graphic = triPreviewPane,
      resultConverter = {
        case ButtonType.OK => resultFromInputs().getOrElse(null)
        case _             => null
      },
      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }
