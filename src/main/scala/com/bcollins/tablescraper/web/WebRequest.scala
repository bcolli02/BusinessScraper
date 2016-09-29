package com.bcollins.tablescraper.web

import scala.io.Source
import scala.util.matching.Regex

trait WebRequest {

  def buildRequest: String
}

case class SBARequest (
  ownerType: String,
  state: String
) extends WebRequest {

  def countyAbbr(str: String): String =
    s"""${str.charAt(0)}${str.charAt(1).toLower}"""

  def buildRequest: String = {
    val reqData: String = Source.fromURL(getClass.getResource("/dsbs_cap")).mkString
    val replMap: Map[Regex, String] =
      Map("""XXXX""".r -> state, """YYYY""".r -> countyAbbr(state), """ZZZZ""".r -> ownerType)

    (reqData /: replMap){ (b: String, a: (Regex, String)) => a._1 replaceAllIn (b, a._2) }
  }
}

case class SBAPageRequest(pageNum: Int) extends WebRequest {

  def buildRequest: String = {
    val reqData: String =
      Source.fromURL(getClass.getResource("/dsbs_subcap")).mkString

    """NNNN""".r replaceAllIn (reqData, pageNum.toString)
  }
}
