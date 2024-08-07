package tripaint.view.gui

import tripaint.coords.StorageCoords
import tripaint.image.ImageStorage
import tripaint.image.format.StorageFormat
import tripaint.view.FileSaveSettings
import tripaint.view.gui.DialogUtils.{getValueFromCustomDialog, makeGridPane}

import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.control.{ChoiceBox, Label, Separator}
import javafx.scene.control.ButtonType
import javafx.scene.image.{Image, ImageView, PixelFormat, WritableImage}
import javafx.scene.layout.{Pane, StackPane}
import javafx.util.StringConverter

import java.io.{File, FileInputStream, FileNotFoundException, IOException}
import scala.util.{Success, Try}

object AskForFileSaveSettingsDialog {
  def askForFileSaveSettings(
      storage: ImageStorage,
      file: File,
      formats: Seq[(StorageFormat, String)],
      initiallySelectedFormat: Int
  ): Option[FileSaveSettings] = {
    val imageSize = storage.imageSize
    val (previewFile, previewWidth, previewHeight) = (file, imageSize, imageSize)

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
      } yield FileSaveSettings(StorageCoords(xOffset, yOffset), format)
    }

    val previewPane = new StackPane
    previewPane.setMinSize(previewWidth, previewHeight)
    previewPane.setMaxSize(previewWidth, previewHeight)

    val previewImage = new WritableImage(imageSize, imageSize)
    val pixelFormat = PixelFormat.getIntArgbInstance
    previewPane.getChildren.add(new ImageView(previewImage))

    val previewBorder = new Pane
    previewBorder.setMinSize(previewWidth, previewHeight)
    previewBorder.setMaxSize(previewWidth, previewHeight)
    previewPane.getChildren.add(previewBorder)

    {
      import javafx.scene.layout.*
      import javafx.scene.paint.Color
      val stroke = new BorderStroke(
        Color.RED,
        BorderStrokeStyle.SOLID,
        CornerRadii.EMPTY,
        BorderWidths.DEFAULT
      )
      previewBorder.setBorder(new Border(stroke))
    }

    val previewStack = new Pane
    try {
      val wholeImage = new ImageView(new Image(new FileInputStream(previewFile)))
      previewStack.getChildren.add(wholeImage)
    } catch {
      case _: FileNotFoundException =>
      case _: IOException           =>
    }

    previewStack.getChildren.add(previewPane)

    def updatePreviewAction(): Unit = {
      resultFromInputs() match {
        case Success(FileSaveSettings(StorageCoords(x, y), format)) =>
          previewPane.setLayoutX(x)
          previewPane.setLayoutY(y)

          val pixels = storage.toRegularImage(format).toIntArray

          val w = previewImage.getPixelWriter
          w.setPixels(0, 0, imageSize, imageSize, pixelFormat, pixels, 0, imageSize)
        case _ =>
      }
    }

    updatePreviewAction()

    xCoordTF.textProperty.addListener(_ => updatePreviewAction())
    yCoordTF.textProperty.addListener(_ => updatePreviewAction())
    formatChooser.getSelectionModel.selectedItemProperty().addListener(_ => updatePreviewAction())

    val inputForm = makeGridPane(
      Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF),
        Seq(new Label("Format:"), formatChooser)
      )
    )

    getValueFromCustomDialog[FileSaveSettings](
      title = "Save file",
      headerText = "Where in the file should the image be saved, and how?",
      content = Seq(
        inputForm,
        Separator(Orientation.HORIZONTAL),
        previewStack
      ),
      resultConverter = {
        case ButtonType.OK => resultFromInputs().getOrElse(null)
        case _             => null
      },
      buttons = Seq(ButtonType.OK, ButtonType.CANCEL)
    )
  }
}
