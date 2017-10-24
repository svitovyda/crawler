package com.svitovyda.crawler.configuration

import com.svitovyda.crawler.CrawlerComponents
import play.api.mvc._


class ConfigurationsController extends Controller {

  private val configurations = ConfigurationsExtension(CrawlerComponents.actorSystem)
  private val settings = configurations.settings

  def showEnvironment = Action {
    Ok(s"${configurations.environment}")
  }

  def showSettings = Action {
    Ok(s"maxPagesToStore: ${settings.maxPagesToStore}, maxDepth: ${settings.maxDepth}")
  }
}
