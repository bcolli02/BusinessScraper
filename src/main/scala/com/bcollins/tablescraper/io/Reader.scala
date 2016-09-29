package com.bcollins.tablescraper.io

import org.apache.commons.lang3.StringEscapeUtils

import scala.io.Source
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.util.matching.Regex
import scala.annotation.tailrec
import scala.language.implicitConversions

case class Row(parameters: List[String]) {

  val length: Int = parameters.size

  /** @inheritdoc */
  override def toString: String = s"""Row [\n  ${parameters.mkString(",\n  ")}\n]"""
}

case class Reader(page: String) {

  implicit def iter2list[A](iter: Iterator[A]): List[A] = iter.toList
  
  private def maxCol(content: String): Int = {
    val NumX: Regex = """Col(\d*)""".r

    (0 /: (for (NumX(num) <- NumX findAllIn content) yield Try(num.toInt) match {
      case Success(n) => n; case Failure(f) => 0
    })){ (max: Int, in: Int) => if (max > in) max else in }
  }

  private def getRows(content: String): List[Row] =
    for (row <- """<tr[\W\w\s]*?>[\W\w\s]*?</tr>""".r findAllIn content) yield
      Row({
        val RowX: Regex = """<td.*?>(.*?)</td>""".r
        val HypRLX: Regex = """<a.*?>(.*?)</a>""".r

        for (RowX(col) <- RowX findAllIn row)
        yield StringEscapeUtils.unescapeHtml4((HypRLX findFirstIn col) match {
          case Some(HypRLX(data)) => data; case None => col
        })
      })

  def collectResults: List[Row] = getRows(page).tail dropRight 1
}
