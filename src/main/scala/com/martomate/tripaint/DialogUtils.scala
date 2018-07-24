package com.martomate.tripaint

import java.io.File

import com.martomate.tripaint.image.{TriImage, TriImageCoords}
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, Region, VBox}

import scala.util.{Failure, Success, Try}


object DialogUtils {
  private def isTrue(pred: => Boolean): Boolean = Try(pred).getOrElse(false)

  def customIntRestriction(pred: Int => Boolean): String => Boolean = s => isTrue(pred(s.toInt)) || isTrue(pred((s + "1").toInt))

  def customDoubleRestriction(pred: Double => Boolean): String => Boolean = s => isTrue(pred(s.toDouble)) || isTrue(pred((s + "1").toDouble))

  val doubleRestriction: String => Boolean = customDoubleRestriction(_ => true)
  val intRestriction: String => Boolean = customIntRestriction(_ => true)
  val uintRestriction: String => Boolean = customIntRestriction(_ >= 0)

  def doubleTF: TextField = makeTF(doubleRestriction)

  def intTF: TextField = makeTF(doubleRestriction)

  def uintTF: TextField = makeTF(doubleRestriction)

  def makeTF(restriction: String => Boolean): TextField = {
    val tf = new TextField
    DialogUtils.restrictTextField(tf, restriction)
    tf
  }

  def restrictTextField(tf: TextField, contentAllowed: String => Boolean): Unit = {
    tf.text.onChange((ob, oldVal, newVal) => {
      if (contentAllowed(newVal)) ob.asInstanceOf[StringProperty].value = newVal
      else ob.asInstanceOf[StringProperty].value = oldVal
    })
  }

  def makeGridPane(content: Seq[Seq[Node]]): GridPane = {
    val gridPane = new GridPane
    gridPane.vgap = 10
    gridPane.hgap = 10
    for (i <- content.indices)
      for (j <- content(i).indices)
        gridPane.add(content(i)(j), j, i)
    gridPane
  }

  def addTitle(title: String, gridPane: GridPane): TitledPane = {
    val pane = new TitledPane {
      this.collapsible = false
      this.text = title
      this.content = gridPane
    }
    pane
  }

  def showInputDialog[R](
                          title: String,
                          headerText: String = null,
                          contentText: String = null,
                          graphic: Node = null,
                          content: Seq[Region] = Seq.empty,
                          resultConverter: ButtonType => R,
                          nodeWithFocus: Node = null,
                          buttons: Seq[ButtonType] = Seq(ButtonType.OK, ButtonType.Cancel)): Option[R] = {

    val dialog = new Dialog[R]
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = graphic
    dialog.dialogPane().setContent(new VBox(content: _*))
    dialog.resultConverter = resultConverter
    for (b <- buttons) dialog.dialogPane().getButtonTypes add b
    if (nodeWithFocus != null) dialog.setOnShowing(_ => Platform.runLater(nodeWithFocus.requestFocus()))
    val result = dialog.delegate.showAndWait()
    if (result.isPresent) Some(result.get) else None
  }

  def askForWhereToPutImage(): Option[(Int, Int)] = {
    val xCoordTF = DialogUtils.uintTF
    val yCoordTF = DialogUtils.uintTF

    showInputDialog[(Int, Int)](
      title = "New image",
      headerText = "Please enter where it should be placed.",

      content = Seq(makeGridPane(Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF)
      ))),

      resultConverter = {
        case ButtonType.OK => Try((xCoordTF.text().toInt, yCoordTF.text().toInt)).getOrElse(null)
        case _ => null
      },

      nodeWithFocus = xCoordTF,

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }

  def askForOffset(): Option[(Int, Int)] = {
    val xCoordTF = DialogUtils.uintTF
    val yCoordTF = DialogUtils.uintTF

    showInputDialog[(Int, Int)](
      title = "Open partial image",
      headerText = "Which part of the image should be opened? Please enter the top left corner:",

      content = Seq(makeGridPane(Seq(
        Seq(new Label("X coordinate:"), xCoordTF),
        Seq(new Label("Y coordinate:"), yCoordTF)
      ))),

      resultConverter = {
        case ButtonType.OK => Try((xCoordTF.text().toInt, yCoordTF.text().toInt)).getOrElse(null)
        case _ => null
      },

      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
  }
}
