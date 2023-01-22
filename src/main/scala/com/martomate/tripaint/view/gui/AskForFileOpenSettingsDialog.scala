package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.view.FileOpenSettings
import com.martomate.tripaint.view.gui.DialogUtils.{getValueFromCustomDialog, makeGridPane}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Orientation
import scalafx.scene.control.{ButtonType, ChoiceBox, Label, Separator}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane
import scalafx.util.StringConverter

import java.io.{File, FileInputStream}
import scala.util.{Success, Try}

object AskForFileOpenSettingsDialog:
  def askForFileOpenSettings(
      imagePreview: (File, Int, Int),
      formats: Seq[(StorageFormat, String)],
      initiallySelectedFormat: Int
  ): Option[FileOpenSettings] = {
    val (previewFile, previewWidth, previewHeight) = imagePreview

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

    val wholeImage = new ImageView(new Image(new FileInputStream(previewFile)))

    val previewStack = new Pane
    previewStack.delegate.getChildren.addAll(wholeImage, previewPane)

    def updatePreviewAction(): Unit =
      resultFromInputs() match
        case Success(FileOpenSettings(StorageCoords(x, y), _)) =>
          previewPane.setLayoutX(x)
          previewPane.setLayoutY(y)

        // TODO: preview TriImage using the format
        case _ =>

    updatePreviewAction()

    xCoordTF.text.onChange(updatePreviewAction())
    yCoordTF.text.onChange(updatePreviewAction())

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
      resultConverter = {
        case ButtonType.OK => resultFromInputs().getOrElse(null)
        case _             => null
      },
      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }
