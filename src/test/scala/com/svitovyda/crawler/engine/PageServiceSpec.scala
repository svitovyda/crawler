package com.svitovyda.crawler.engine

import com.svitovyda.crawler.engine.SearchService.SearchPageResult
import com.svitovyda.crawler.engine.UrlService.Url
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.concurrent.ScalaFutures

import org.scalatest.{FlatSpec, Inspectors, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.ScalaFutures


class PageServiceSpec extends FlatSpec with Matchers with Inspectors with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(
    timeout = scaled(3000.millis),
    interval = scaled(100.millis)
  )

  it should "fail on non-text/html page" in {
    val f = Await.ready(PageService.addUrl(
      Url("https://www.scala-lang.org/resources/img/frontpage/scala-logo-white@2x.png")), 3.seconds)
    whenReady(f.failed) { e =>
      e shouldBe a [RuntimeException]
    }
  }

  it should "return text and same-domain links on html/text page" in {
    val page = Await.result(PageService.addUrl(Url("http://www.scala-lang.org/")), 3.seconds)
    page.text.nonEmpty should be (true)
    page.urls.nonEmpty should be (true)
    page.urls.forall(_.value.startsWith("http://www.scala-lang.org"))
  }
}
