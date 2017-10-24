package com.svitovyda.crawler.engine

import java.net.MalformedURLException

import scala.util.Try

object UrlService {

  case class Url(value: String) extends AnyVal

  def validateUrl(url: String): Either[String, Url] =
    Try(new java.net.URI(url)).map(_ => Right(Url(url))).recover {
      case e: MalformedURLException => Left(s"Invalid URL: ${e.getMessage}")
      case e: Exception => Left(s"Unknown error: ${e.getMessage}")
    }.get

  def isSameDomain(base: Url, target: Url): Boolean = {
    val targetHost = new java.net.URI(target.value).getHost
    val baseHost = new java.net.URI(base.value).getHost
    baseHost == targetHost
  }
}
