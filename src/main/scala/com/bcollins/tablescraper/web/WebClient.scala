package com.bcollins.tablescraper.web

import scalaz.\/
import scalaz.-\/
import scalaz.\/-
import scalaz.concurrent.Task
import scala.annotation.tailrec
import scala.util.matching.Regex

import org.http4s.Method
import org.http4s.MediaType
import org.http4s.Header
import org.http4s.Headers
import org.http4s.EmptyBody
import org.http4s.EntityEncoder
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import org.http4s.headers.`Content-Type`

import com.bcollins.tablescraper.io.Reader
import com.bcollins.tablescraper.io.Row

object WebClient {

  /** Http4s Blaze client instance */
  val client = org.http4s.client.blaze.defaultClient

  /** HTTP Request method */
  val method: Method = Method.POST

  /** HTTP Request header */
  val header: Header = `Content-Type`(MediaType.`application/x-www-form-urlencoded`)

  /** Regex for grabbing session cookies from response. */
  val RespX: Regex = """(Set-cookie:) (JSESSIONID.*?=/)""".r

  /** Target sites. */
  val targets: Seq[String] = Seq (
    "dsp_dsbs.cfm",
    "dsp_dsbs.cfm&CleaningPass=Cookies",
    "dsp_profilelist.cfm?requesttimeout=180",
    "dsp_profilelist.cfm?requesttimeout=180&CleaningPass=Cookies"
  ) map ("http://dsbs.sba.gov/dsbs/search/" + _)
  
  def grabPages(req: Option[WebRequest]): Throwable \/ List[Row] =
    execute(None, 0) match {
      case \/-(s) => \/-(Reader(s).collectResults ++ grabNextPages(1501, None))
      case -\/(f) => RespX findFirstIn f match {
        case Some(RespX(setter, cookie)) =>
          val cookies: Option[List[Header]] =
            Some(List(Header("Cookie", cookie)))

          execute(None, 1, cookies) match {
            case \/-(s) =>
              \/-(Reader(s).collectResults ++ grabNextPages(501, cookies))
            case -\/(f) => execute(req, 2, cookies) match {
              case \/-(ss) =>
                \/-(Reader(ss).collectResults ++ grabNextPages(501, cookies))
              case -\/(ff) => execute(req, 3, cookies) match {
                case \/-(sss) =>
                  \/-(Reader(sss).collectResults ++ grabNextPages(501, cookies))
                case -\/(fff) => -\/(new Throwable(fff))
              }
            }
          }
        case None => -\/(new Throwable("No session cookies response."))
      }
    }

  private def grabNextPages(n: Int,
    cookies: Option[List[Header]]): List[Row] = {
    val req: Option[WebRequest] = Some(SBAPageRequest(n))

    execute(req, 2, cookies) match {
      case \/-(ss) => Reader(ss).collectResults match {
        case Nil => Nil
        case ls@(x :: xs) if (ls.size <= 2) => Nil
        case ls@(x :: xs) if (ls.size > 2) =>
          ls ++ grabNextPages(n + 500, cookies)
      }
      case -\/(ff) => execute(req, 3, cookies) match {
        case \/-(sss) => Reader(sss).collectResults match {
          case Nil => Nil
          case ls => ls ++ grabNextPages(n + 500, cookies)
        }
        case -\/(fff) => Nil
      }
    }
  }

  private def execute(req: Option[WebRequest], extNum: Int,
    headrs: Option[List[Header]] = None): String \/ String =
    makeCall(req, extNum, headrs) match {
      case \/-(resp) => \/-(resp.as[String].run)
      case -\/(f) => -\/(f)
    }

  private def makeCall(req: Option[WebRequest], extNum: Int,
    headrs: Option[List[Header]] = None): String \/ Response =
    makeRequest(req, extNum, headrs) match {
      case \/-(r) => makeResponse(r)
      case -\/(f) => -\/(f)
    }

  private def makeRequest(req: Option[WebRequest], extNum: Int,
    headrs: Option[List[Header]] = None): String \/ Task[Request] =
    Uri.fromString(targets(extNum)).toOption match {
      case Some(url) =>
        \/-(EntityEncoder[String].toEntity(req match {
          case Some(r) => r.buildRequest
          case None => ""
        }) map { entity =>
          Request(
            method = if (req.isDefined) this.method else Method.GET,
            headers = Headers(header :: headrs.getOrElse(Nil)),
            uri = url,
            body = entity.body
          )
        })
      case None => -\/("Bad url.")
    }

  private def makeResponse(req: Task[Request]): String \/ Response = {
    val response: Response = client(req).run
    val status: Boolean = statusOk(response)

    status match {
      case true => \/-(response)
      case false => -\/(response.toString())
    }
  }

  private def statusOk(resp: Response): Boolean =
    resp.status.equals(Status.Ok)
}
