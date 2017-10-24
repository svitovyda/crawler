package com.svitovyda.crawler

import akka.actor.ActorSystem
import play.api.{ApplicationLoader, BuiltInComponentsFromContext}
import play.api.ApplicationLoader.Context
import play.api.routing.Router

import com.svitovyda.crawler.configuration.ConfigurationsComponents
import com.svitovyda.crawler.engine.EngineComponents
import com.svitovyda.crawler.router.RouterComponents


class CrawlerComponents(context: Context) {
  import CrawlerComponents._

  lazy val counter = new EngineComponents

  lazy val configurations = new ConfigurationsComponents

  lazy val router = new RouterComponents(counter, configurations)

  lazy val play = new Play(context, () => router.router)
}

object CrawlerComponents {

  val actorSystem = ActorSystem()

  class Play(
      context: ApplicationLoader.Context,
      createRouter: () => Router)
    extends BuiltInComponentsFromContext(context) {

    lazy val router: Router = createRouter()
    override lazy val actorSystem: ActorSystem = CrawlerComponents.actorSystem
  }
}
