package com.svitovyda.crawler.engine

import com.svitovyda.crawler.engine.SearchService.SearchPageResult
import com.svitovyda.crawler.engine.UrlService.Url
import org.scalatest.{FlatSpec, Inspectors, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class SearchServiceSpec extends FlatSpec with Matchers with Inspectors {
  it should "find one document" in {
    Await.result(
      SearchService.search("abc", Map(Url("http://google.com") -> "1 2 3 4 5 abc 6 7 8 9 0")),
      1.second
    ) should be (List(SearchPageResult(Url("http://google.com"), List("1 2 3 4 5 abc 6 7 8 9 0"))))
  }

  it should "not find any documents" in {
    Await.result(
      SearchService.search("xyz", Map(Url("http://google.com") -> "1 2 3 4 5 abc 6 7 8 9 0")),
      1.second
    ) should be (List())
  }

  it should "correctly trim surrounding text" in {
    val text = "zzz12345678901234567890abc12345678901234567890zzz"
    Await.result(
      SearchService.search("abc", Map(Url("http://google.com") -> text)),
      1.second
    ) should be (List(SearchPageResult(
      Url("http://google.com"),
      List("12345678901234567890abc12345678901234567890"))))
  }
}
