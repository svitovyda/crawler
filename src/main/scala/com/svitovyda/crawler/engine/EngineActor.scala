package com.svitovyda.crawler.engine

import java.time.{LocalDateTime, ZoneOffset}

import akka.actor._
import com.svitovyda.crawler.CrawlerComponents
import com.svitovyda.crawler.configuration.ConfigurationsExtension
import com.svitovyda.crawler.engine.EngineActor.Response.SearchResult
import com.svitovyda.crawler.engine.EngineActor._
import com.svitovyda.crawler.engine.PageService.ScrapingResult
import com.svitovyda.crawler.engine.SearchService.SearchPageResult
import com.svitovyda.crawler.engine.UrlService.Url

import scala.concurrent.ExecutionContext


class EngineActor(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  var storage: Map[Url, PageState] = Map()

  private val settings = ConfigurationsExtension(CrawlerComponents.actorSystem).settings
  private val maxSize = settings.maxPagesToStore
  private val maxDepth = if(settings.maxDepth == -1) Int.MaxValue else settings.maxDepth


  override def receive: Receive = {

    case Request.GetCount =>
      sender() ! Response.Count(storage.size)

    case Request.GetUrls =>
      sender() ! Response.URLs(storage.keys.toList)

    case Request.GetPageContent(url) =>
      if(! withExistingPage(Url(url)) { page =>
        if(page.status == PageStatus.Ready)
          sender() ! Response.Content(page.text)
        else sender() ! s"The page loading is in a process"
      }) sender() ! s"Page doesn't exist"

    case Request.AddUrl(url) =>
      val receiver = sender()
      UrlService.validateUrl(url).fold(
        error =>
          receiver ! error,
        url => {
          if(!storage.contains(url)) {
            log info s"Adding page: $url"
            storage += url -> PageState(url)

            PageService.addUrl(url).map { case ScrapingResult(text, urls) =>
              self ! Command.UpdatePageState(url, text)
              urls foreach withAllowedUrlAndDepth(url, 1) { allowed =>
                self ! Command.AddSubUrl(allowed)
              }
              receiver ! Response.Count(storage.size)
            }.recover { case e: Exception =>
              receiver ! s"Could not add url $url, ${e.getMessage}"
              self ! Command.RemoveUrl(url)
            }
          }
          else receiver ! s"The URL $url is already added!"
        }
      )

    case Command.AddSubUrl(url, depth) =>
      storage += url -> PageState(url)
      PageService.addUrl(url).map { case ScrapingResult(text, urls) =>
        self ! Command.UpdatePageState(url, text)
        urls foreach withAllowedUrlAndDepth(url, depth + 1) { allowed =>
          self ! Command.AddSubUrl(allowed)
        }
      }.recover { case _ => self ! Command.RemoveUrl(url) }

    case Command.UpdatePageState(url, text) =>
      withExistingPage(url) { page =>
        storage = storage.updated(url, page.copy(status = PageStatus.Ready, text = text))
        checkMemory()
      }

    case Command.RemoveUrl(url) =>
      withExistingPage(url) { _ => storage -= url }

    case Request.Search(query) =>
      val receiver = sender()
      val loaded = storage.collect {
        case (url, PageState(_, PageStatus.Ready, text, _)) => url -> text
      }
      SearchService.search(query, loaded).map(receiver ! SearchResult(_))
  }

  private def withAllowedUrlAndDepth(baseUrl: Url, depth: Int)(f: Url => Unit)(targetUrl: Url) =
    if(!storage.contains(targetUrl) &&
      UrlService.isSameDomain(baseUrl, targetUrl) &&
      depth <= maxDepth
    ) f(targetUrl)

  private def withExistingPage(url: Url)(f: PageState => Unit): Boolean = {
    val page = storage.get(url)
    page foreach f
    page.isDefined
  }

  private def checkMemory() = if(storage.size > maxSize) {
    val urlToDelete = storage.values.toList.min(Ordering.by{ p: PageState =>
      p.dateAdded.toEpochSecond(ZoneOffset.UTC)
    }).url
    storage -= urlToDelete
  }
}

object EngineActor {
  def props(ec: ExecutionContext): Props = Props(classOf[EngineActor], ec)

  sealed trait PageStatus
  object PageStatus {
    case object Loading extends PageStatus
    case object Ready extends PageStatus
    case object Unknown extends PageStatus
  }

  case class PageState(
    url: Url,
    status: PageStatus = PageStatus.Loading,
    text: String = "",
    dateAdded: LocalDateTime = LocalDateTime.now
  )

  sealed trait Request
  object Request {
    case object GetCount extends Request
    case class AddUrl(url: String) extends Request
    case class Search(phrase: String) extends Request
    case object GetUrls extends Request
    case class GetPageContent(url: String) extends Request
  }

  sealed trait Response
  object Response {
    case class Count(count: Int) extends Response
    case class URLs(list: List[Url]) extends Response
    case class Content(text: String) extends Response
    case class SearchResult(results: List[SearchPageResult]) extends Response
  }

  sealed trait Command
  object Command {
    case class UpdatePageState(Url: Url, text: String) extends Command
    case class AddSubUrl(url: Url, depth: Int = 1) extends Command
    case class RemoveUrl(url: Url) extends Command
  }
}
