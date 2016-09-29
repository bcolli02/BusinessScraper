package com.bcollins.tablescraper

import java.io.File

import scalaz.\/
import scalaz.-\/
import scalaz.\/-
import scala.io.Source

import com.bcollins.tablescraper.web.WebClient
import com.bcollins.tablescraper.web.WebRequest
import com.bcollins.tablescraper.web.SBARequest
import com.bcollins.tablescraper.io.Reader
import com.bcollins.tablescraper.io.Row
import com.bcollins.tablescraper.io.TSWriter

object Driver {

  def main(args: Array[String]): Unit = TSWriter("xls", None)
}
