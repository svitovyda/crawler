package com.svitovyda.crawler.engine

import com.svitovyda.crawler.engine.UrlService.Url
import scala.concurrent.{ExecutionContext, Future}


object SearchService {

  val CharsAround = 20

  case class SearchPageResult(url: Url, contexts: List[String])

  // in current implementation returns only first occurrence in the text
  def search(query: String, pages: Map[Url, String])
      (implicit ec: ExecutionContext): Future[List[SearchPageResult]] = Future {
    val queryAround = query.length + CharsAround
    pages.foldLeft(List[SearchPageResult]()) { case (z, (url, text)) =>
      text.indexOf(query) match {
        case -1 => z
        case i =>
          val start = math.max(0, i - CharsAround)
          val end = math.min(text.length, i + queryAround)
          SearchPageResult(url, List(text.substring(start, end))) :: z
      }
    }
  }

}
