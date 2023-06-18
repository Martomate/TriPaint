package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.ImageGrid
import com.martomate.tripaint.model.coords.{StorageCoords, GridCoords}
import com.martomate.tripaint.model.image.{GridCell, ImagePool, ImageStorage}
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.view.gui.DialogUtils.{getValueFromCustomDialog, makeGridPane}
import com.martomate.tripaint.view.image.TriImageForPreview
import com.martomate.tripaint.view.{FileOpenSettings, FileSaveSettings}
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Orientation
import scalafx.scene.Node
import scalafx.scene.control.*
import scalafx.scene.image.{Image, ImageView, PixelFormat, WritableImage}
import scalafx.scene.layout.*
import scalafx.util.StringConverter

import java.io.{File, FileInputStream, FileNotFoundException, IOException}
import scala.util.{Success, Try}

type TextFieldRestriction = String => Boolean

object TextFieldRestriction:
  private def isTrue(pred: => Boolean): Boolean = Try(pred).getOrElse(false)

  def customIntRestriction(pred: Int => Boolean): TextFieldRestriction = s =>
    isTrue(pred(s.toInt)) || isTrue(pred((s + "1").toInt))

  val intRestriction: TextFieldRestriction = customIntRestriction(_ => true)
  val uintRestriction: TextFieldRestriction = customIntRestriction(_ >= 0)

object RestrictedTextField:
  def intTF: TextField = restrict(new TextField, TextFieldRestriction.intRestriction)
  def uintTF: TextField = restrict(new TextField, TextFieldRestriction.uintRestriction)

  def restrict(tf: TextField, contentAllowed: TextFieldRestriction): TextField =
    tf.text.onChange((ob, oldVal, newVal) => {
      ob.asInstanceOf[StringProperty].value = if contentAllowed(newVal) then newVal else oldVal
    })
    tf

object DialogUtils:
  def makeGridPane(content: Seq[Seq[Node]]): GridPane =
    val gridPane = new GridPane
    gridPane.vgap = 10
    gridPane.hgap = 10
    for
      i <- content.indices
      j <- content(i).indices
    do gridPane.add(content(i)(j), j, i)
    gridPane

  def getValueFromCustomDialog[R](
      title: String,
      headerText: String = null,
      contentText: String = null,
      graphic: Node = null,
      content: Seq[Region] = Seq.empty,
      resultConverter: ButtonType => R,
      nodeWithFocus: Node = null,
      buttons: Seq[ButtonType] = Seq(ButtonType.OK, ButtonType.Cancel)
  ): Option[R] =
    val dialog = new Dialog[R]
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = graphic

    val contentBox = new VBox(content: _*)
    contentBox.setSpacing(10)

    dialog.dialogPane().setContent(contentBox)
    dialog.resultConverter = resultConverter

    for b <- buttons do dialog.dialogPane().getButtonTypes.add(b)

    if nodeWithFocus != null then
      dialog.setOnShowing(_ => Platform.runLater(nodeWithFocus.requestFocus()))

    val result = dialog.delegate.showAndWait()
    if result.isPresent then Some(result.get) else None

  def getValueFromDialog[T](
      imagePool: ImagePool,
      images: Seq[GridCell],
      title: String,
      headerText: String,
      contentText: String,
      restriction: String => Boolean,
      stringToValue: String => T,
      refreshPreviewFn: Option[(T, ImageGrid) => Unit] = None
  ): Option[T] =
    val (previewPane, updatePreview) = makeImagePreviewList(images, imagePool)

    val dialog = new TextInputDialog
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = previewPane
    RestrictedTextField.restrict(dialog.editor, restriction)

    refreshPreviewFn match
      case Some(refreshFn) =>
        dialog.editor.text.onChange((_, _, s) =>
          if restriction(s) then
            val value = stringToValue(s)
            updatePreview(imageGrid => refreshFn(value, imageGrid))
          else updatePreview(_ => ())
        )
      case None =>

    dialog.showAndWait() match
      case Some(str) =>
        val num = stringToValue(str)
        Some(num)
      case None =>
        None

  def makeImagePreviewList(
      images: Seq[GridCell],
      imagePool: ImagePool
  ): (ScrollPane, (ImageGrid => Unit) => Unit) =
    ImagePreviewList.fromImageContent(images, TriImageForPreview.previewSize, imagePool.locationOf)
