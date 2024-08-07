package tripaint.view.gui

import tripaint.Color
import tripaint.coords.{GridCoords, StorageCoords}
import tripaint.grid.{GridCell, ImageGrid}
import tripaint.image.{ImageStorage, RegularImage}
import tripaint.image.format.StorageFormat
import tripaint.view.FileOpenSettings
import tripaint.view.gui.DialogUtils.{getValueFromCustomDialog, makeGridPane}
import tripaint.view.image.TriImageForPreview

import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Orientation
import javafx.scene.control.{ChoiceBox, Label, Separator}
import javafx.scene.control.ButtonType
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.util.StringConverter

import java.io.File
import scala.util.{Success, Try}

object AskForFileOpenSettingsDialog {
  def askForFileOpenSettings(
      imagePreview: (File, Int, Int, Int),
      formats: Seq[(StorageFormat, String)],
      initiallySelectedFormat: Int,
      readImage: File => Option[RegularImage]
  ): Option[FileOpenSettings] = {
    val (previewFile, imageSize, xCount, yCount) = imagePreview
    val (previewWidth, previewHeight) = (xCount * imageSize, yCount * imageSize)

    val xCoordTF = RestrictedTextField.uintTF
    xCoordTF.setPromptText("0")

    val yCoordTF = RestrictedTextField.uintTF
    yCoordTF.setPromptText("0")

    val formatMap: Map[StorageFormat, String] = Map.from(formats)

    val formatChooser = {
      val b = new ChoiceBox[StorageFormat](FXCollections.observableArrayList(formats.map(_._1)*))
      b.getSelectionModel.select(initiallySelectedFormat)
      b.setConverter(new StringConverter[StorageFormat] {
        override def toString(t: StorageFormat) = formatMap(t)
        override def fromString(s: String): StorageFormat =
          throw new UnsupportedOperationException()
      })
      b
    }

    val resultFromInputs = () => {
      val xt = xCoordTF.getText()
      val yt = yCoordTF.getText()
      val format = formatChooser.getSelectionModel.getSelectedItem

      for {
        xOffset <- Try(if xt != "" then xt.toInt else 0)
        yOffset <- Try(if yt != "" then yt.toInt else 0)
      } yield FileOpenSettings(StorageCoords(xOffset, yOffset), format)
    }

    val previewPaneBorder = {
      import javafx.scene.layout.*
      import javafx.scene.paint.Color
      val stroke = new BorderStroke(
        Color.RED,
        BorderStrokeStyle.SOLID,
        CornerRadii.EMPTY,
        BorderWidths.DEFAULT
      )
      new Border(stroke)
    }

    val previewPane = {
      val p = new Pane
      p.setMinSize(previewWidth, previewHeight)
      p.setMaxSize(previewWidth, previewHeight)
      p.setBorder(previewPaneBorder)
      p
    }

    val underlyingImage = readImage(previewFile).get

    val wholeImage = new ImageView(SwingFXUtils.toFXImage(underlyingImage.toBufferedImage, null))

    val previewStack = new Pane
    previewStack.getChildren.addAll(wholeImage, previewPane)

    val blankImage = ImageStorage.fill(imageSize, Color.White)
    val imageGridCells = Seq.tabulate(xCount)(x => new GridCell(GridCoords(x, 0), blankImage))
    val imageGrid = ImageGrid.fromCells(imageSize, imageGridCells)

    val (triPreviewPane, updateTriPreview) = ImagePreviewList.fromImageContent(
      imageGrid.images,
      TriImageForPreview.previewSize,
      _ => None
    )

    def updatePreviewAction(): Unit = {
      resultFromInputs() match {
        case Success(FileOpenSettings(StorageCoords(sx, sy), format)) =>
          previewPane.setLayoutX(sx)
          previewPane.setLayoutY(sy)

          for x <- 0 until xCount do {
            val offset = StorageCoords(sx + x * imageSize, sy)
            val newPreviewImage = ImageStorage
              .fromRegularImage(underlyingImage, offset, format, imageSize)
              .getOrElse(blankImage)

            imageGrid.replaceImage(GridCoords(x, 0), newPreviewImage)

            updateTriPreview(_ => ())
          }
        case _ =>
      }
    }

    updatePreviewAction()

    xCoordTF.textProperty.addListener(_ => updatePreviewAction())
    yCoordTF.textProperty.addListener(_ => updatePreviewAction())
    formatChooser.setOnAction(_ => updatePreviewAction())

    val inputForm = makeGridPane(
      Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF),
        Seq(new Label("Format:"), formatChooser)
      )
    )

    getValueFromCustomDialog[FileOpenSettings](
      title = "Open image",
      headerText = "Which part of the image should be opened? Please enter the top left corner:",
      content = Seq(
        inputForm,
        Separator(Orientation.HORIZONTAL),
        previewStack
      ),
      graphic = triPreviewPane,
      resultConverter = {
        case ButtonType.OK => resultFromInputs().getOrElse(null)
        case _             => null
      },
      buttons = Seq(ButtonType.OK, ButtonType.CANCEL)
    )
  }
}
