package com.svitovyda.crawler.router

import com.svitovyda.crawler.CrawlerComponents
import com.svitovyda.crawler.configuration.ConfigurationsComponents
import com.svitovyda.crawler.engine.EngineComponents
import org.webjars.RequireJS
import play.api.http.MimeTypes
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing._
import play.api.routing.sird._

import scala.concurrent.ExecutionContext


class RouterComponents(
  engine: EngineComponents,
  configurations: ConfigurationsComponents
) {

  implicit val context: ExecutionContext = CrawlerComponents.actorSystem.dispatcher

  lazy val router: Router = Router.from {

    case GET(p"/setup.js") => Action {
      Ok(RequireJS.getSetupJavaScript("/webjars/")).as(MimeTypes.JAVASCRIPT)
    }
    case GET(p"/assets/$file*") => controllers.Assets.at(path = "/public", file)
    case GET(p"/webjars/$file*") => controllers.WebJarAssets.at(file)

    case GET(p"/") => Action { Found("/assets/index.html") }

    case GET(p"/environment") => configurations.controller.showEnvironment
    case GET(p"/settings") => configurations.controller.showSettings

    case GET(p"/hello/$to") => Action { Results.Ok(s"Hello $to") }

    case GET(p"/pages/count") => engine.controller.getPagesCount
    case GET(p"/pages/urls") => engine.controller.getAllUrls
    case GET(p"/pages/content") => engine.controller.getPageContent

    case POST(p"/pages") => engine.controller.addUrl()
    case GET(p"/pages/search") => engine.controller.search
  }

}
