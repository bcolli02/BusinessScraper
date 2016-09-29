package com.bcollins.tablescraper.io

import java.io.File
import java.io.FileFilter
import java.io.PrintWriter

import scalaz.\/
import scalaz.-\/
import scalaz.\/-
import scala.Enumeration
import info.folone.scala.poi
import info.folone.scala.poi.Sheet
import info.folone.scala.poi.Cell
import info.folone.scala.poi.StringCell
import info.folone.scala.poi.Workbook
import com.typesafe.scalalogging.LazyLogging

import com.bcollins.tablescraper.States
import com.bcollins.tablescraper.BusinessTypes
import com.bcollins.tablescraper.web.WebRequest
import com.bcollins.tablescraper.web.WebClient
import com.bcollins.tablescraper.web.SBARequest

object TSWriter {

  object FormatTypes extends Enumeration {
    type Format = Value
    val plain, xls, xml, json = Value
  }

  def apply(format: String, file: Option[File]): Unit =
    FormatTypes.withName(format) match {
      case FormatTypes.plain => PlainWriter.write(file)
      case FormatTypes.xls => XLSWriter.write(file)
      case FormatTypes.xml => XMLWriter.write(file)
      case FormatTypes.json => JSONWriter.write(file)
    }
}

trait TSWriter extends LazyLogging {

  val saveDirectory: String = "~/Documents/BusinessTables/"

  def validate(fn: String): File = {
    val file: File = new File(saveDirectory + fn)
    file.getParentFile().mkdirs()

    file
  }

  def write(file: Option[File]): Unit
}

case object PlainWriter extends TSWriter {

  def write(file: Option[File]): Unit = {
    val writer: PrintWriter =
      new PrintWriter(validate("plain_results.txt"))

    //rows.foreach(writer.println)
  }
}

case object XLSWriter extends TSWriter {

  case class State(abbr: String, name: String)

  private def makeRow(row: List[String], n: Int = 0,
    out: Set[Cell] = Set()): Set[Cell] = row match {
    case Nil => out
    case x :: xs =>
      makeRow(xs, n + 1, out + StringCell(n, x))
  }

  private def makeRows(rows: List[Row], n: Int = 0, out: Set[poi.Row] = Set()):
      Set[poi.Row] = rows match {
    case Nil => out
    case x :: xs =>
      makeRows(xs, n + 1, out + poi.Row(n) { makeRow(x.parameters) })
  }

  private def makeSheet(rows: List[Row], name: String): Sheet =
    Sheet(name) { makeRows(rows) }

  private def makeWorkbook(ownerType: String): Workbook = Workbook {
    (Set[Sheet]() /: States.values) { (b: Set[Sheet], a: States.Value) =>
      val request: Option[WebRequest] = Some(SBARequest(ownerType, a.toString()))

      WebClient.grabPages(request) match {
        case \/-(ls) => println(s"""Fetched ${a.toString()}.""")
          b + makeSheet(ls, a.toString())
        case -\/(f) => println(s"""Could not fetch ${a.toString()}."""); b
      }
    }
  }

  def write(file: Option[File]): Unit =
    BusinessTypes.values.foreach { v => makeWorkbook(v.toString()).
      safeToFile(s"""/home/brennan/Documents/${v}_results.xls""").
      fold(ex => throw ex, identity).unsafePerformIO
    }
}

case object XMLWriter extends TSWriter {

  def write(file: Option[File]): Unit = {

  }
}

case object JSONWriter extends TSWriter {

  def write(file: Option[File]): Unit = {

  }
}
