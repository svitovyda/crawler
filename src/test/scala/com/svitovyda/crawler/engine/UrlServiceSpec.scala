package com.svitovyda.crawler.engine

import com.svitovyda.crawler.engine.UrlService.Url
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class UrlServiceSpec extends FlatSpec with Matchers with Inspectors {

  it should "validate correct url" in {
    UrlService.validateUrl("https://www.scala-lang.org/") should be (Right(Url("https://www.scala-lang.org/")))
  }

  it should "return error on malformed URL" in {
    UrlService.validateUrl("s c a l a o r g") should be (a[Left[String, Url]])
  }

  it should "detect same domain" in {
    UrlService.isSameDomain(
      Url("https://www.scala-lang.org/download/"),
      Url("https://www.scala-lang.org/community/")) should be (true)
  }

  it should "detect different domains" in {
    UrlService.isSameDomain(
      Url("https://www.scala-lang.org/download/"),
      Url("http://google.com/")) should be (false)
  }
}
