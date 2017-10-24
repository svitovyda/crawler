package com.svitovyda.crawler.engine

import com.svitovyda.crawler.engine.UrlService.Url
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.concurrent.{ExecutionContext, Future}


object PageService {
  case class ScrapingResult(text: String, urls: List[Url])

  def addUrl(targetUrl: Url)(implicit ec: ExecutionContext): Future[ScrapingResult] = Future {
    val response = Jsoup.connect(targetUrl.value).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1").execute()

    require(response.contentType.startsWith("text/html"))

    val doc = response.parse()
    val links = doc.select("a[href]").toArray
      .map(u => UrlService.validateUrl(u.asInstanceOf[Element].attr("href")))
      .collect { case Right(url) if UrlService.isSameDomain(targetUrl, url) => url }

    ScrapingResult(doc.text(), links.toList)
  }
}
