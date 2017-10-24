package com.svitovyda.crawler.engine

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.svitovyda.crawler.CrawlerComponents
import com.svitovyda.crawler.engine.EngineController._
import com.svitovyda.crawler.engine.SearchService.SearchPageResult
import com.svitovyda.crawler.engine.UrlService.Url
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, Controller, _}
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.matching.Regex


class EngineController extends Controller {

  val holder: ActorRef = EngineExtension(CrawlerComponents.actorSystem).counterHolder

  implicit val timeout: Timeout = 5.seconds
  implicit val context: ExecutionContext = CrawlerComponents.actorSystem.dispatcher

  def getPagesCount: Action[AnyContent] = Action.async {
    (holder ? EngineActor.Request.GetCount).map {
      case EngineActor.Response.Count(n) => Ok(Json.toJson(CounterResponse(n)))
      case _ => ExpectationFailed
    }
  }

  def getAllUrls: Action[AnyContent] = Action.async {
    (holder ? EngineActor.Request.GetUrls).map {
      case EngineActor.Response.URLs(list) => Ok(Json.toJson(UrlsResponse(list.map(_.value))))
      case _ => ExpectationFailed
    }
  }

  def getPageContent: Action[ContentRequest] = Action.async(parse.json[ContentRequest]) { implicit request =>
    (holder ? EngineActor.Request.GetPageContent).map {
      case EngineActor.Response.Content(text) => Ok(Json.toJson(ContentResponse(text)))
      case _ => ExpectationFailed
    }
  }

  def addUrl(): Action[UrlRequest] = Action.async(parse.json[UrlRequest]) { implicit request =>
    (holder ? EngineActor.Request.AddUrl(request.body.url)).map {
      case EngineActor.Response.Count(n) => Ok(Json.toJson(CounterResponse(n)))
      case error: String => NotAcceptable(error)
      case _ => ExpectationFailed
    }
  }

  def search: Action[SearchRequest] =
    Action.async(parse.json[SearchRequest]) { implicit request =>
      (holder ? EngineActor.Request.Search(request.body.query)).map {
        case EngineActor.Response.SearchResult(list) => Ok(Json.toJson(SearchResponse(list)))
        case error: String => NotAcceptable(error)
        case _ => ExpectationFailed
      }
    }
}

object EngineController {
  val UrlValidationPattern: Regex = "@^(https?|ftp)://[^\\s/$.?#].[^\\s]*$@iS".r

  implicit val urlWrites: Writes[Url] = Writes(u => JsString(u.value))

  case class CounterResponse(count: Int)
  object CounterResponse {
    implicit val writes: Writes[CounterResponse] = Json.writes[CounterResponse]
  }

  case class UrlsResponse(list: List[String])
  object UrlsResponse {
    implicit val writes: Writes[UrlsResponse] = Json.writes[UrlsResponse]
  }

  case class ContentResponse(text: String)
  object ContentResponse {
    implicit val writes: Writes[ContentResponse] = Json.writes[ContentResponse]
  }

  case class SearchResponse(list: List[SearchPageResult])
  object SearchResponse {
    implicit val resultWrites: Writes[SearchPageResult] = Json.writes[SearchPageResult]
    implicit val writes: Writes[SearchResponse] = Json.writes[SearchResponse]
  }

  case class UrlRequest(url: String)
  object UrlRequest {
    implicit val readsRequest: Reads[UrlRequest] = Reads { json =>
      for {
        url <- (json \ "url").validate[String](Reads.pattern(UrlValidationPattern))
      } yield UrlRequest(url)
    }
  }

  case class ContentRequest(url: String)
  object ContentRequest {
    implicit val readsRequest: Reads[ContentRequest] = Reads { json =>
      for {
        url <- (json \ "url").validate[String](Reads.pattern(UrlValidationPattern))
      } yield ContentRequest(url)
    }
  }

  case class SearchRequest(query: String)
  object SearchRequest {
    implicit val readsRequest: Reads[SearchRequest] = Reads { json =>
      for {
        query <- (json \ "query").validate[String](Reads.minLength[String](4))
      } yield SearchRequest(query)
    }
  }
}
