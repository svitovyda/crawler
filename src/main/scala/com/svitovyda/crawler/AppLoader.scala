package com.svitovyda.crawler

import java.util.TimeZone

import play.api._
import play.api.ApplicationLoader.Context


class AppLoader extends ApplicationLoader {

  System.setProperty("user.timezone", "UTC")
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  def load(context: Context): Application = {
    new CrawlerComponents(context).play.application
  }
}
