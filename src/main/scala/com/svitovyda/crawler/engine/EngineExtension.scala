package com.svitovyda.crawler.engine

import akka.actor._
import com.svitovyda.crawler.CrawlerComponents

import scala.concurrent.ExecutionContext

class EngineExtensionImpl(system: ExtendedActorSystem) extends Extension {

  val ec: ExecutionContext = CrawlerComponents.actorSystem.dispatchers.lookup("crawler.load-context")

  val counterHolder: ActorRef = system.actorOf(EngineActor.props(ec), "engine")
}

object EngineExtension
  extends ExtensionId[EngineExtensionImpl]
    with ExtensionIdProvider {

  override def lookup() = EngineExtension

  override def createExtension(system: ExtendedActorSystem) = new EngineExtensionImpl(system)

  override def get(system: ActorSystem): EngineExtensionImpl = super.get(system)
}
