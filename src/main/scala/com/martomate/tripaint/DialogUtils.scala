package com.martomate.tripaint

import scalafx.beans.property.StringProperty
import scalafx.scene.Node
import scalafx.scene.control.{ButtonType, Dialog, TextField, TitledPane}
import scalafx.scene.layout.{GridPane, Region, VBox}

import scala.util.Try


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
                          buttons: Seq[ButtonType] = Seq(ButtonType.OK, ButtonType.Cancel)): Option[R] = {

    val dialog = new Dialog[R]
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    dialog.graphic = graphic
    dialog.dialogPane().setContent(new VBox(content: _*))
    dialog.resultConverter = resultConverter
    for (b <- buttons) dialog.dialogPane().getButtonTypes add b

    val result = dialog.delegate.showAndWait()
    if (result.isPresent) Some(result.get) else None
  }
}