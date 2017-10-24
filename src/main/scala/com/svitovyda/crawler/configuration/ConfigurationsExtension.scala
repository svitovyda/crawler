package com.svitovyda.crawler.configuration

import akka.actor._
import com.typesafe.config.{Config, ConfigFactory}


class ConfigurationsExtensionImpl extends Extension {

  val environment: Option[String] = Option(System.getenv("ENVIRONMENT"))

  private val config: Config = environment match {
    case Some(name) => ConfigFactory.load(s"environments/$name")
    case None => ConfigFactory.load("application")
  }

  val settings: Settings = Settings(config)
}

object ConfigurationsExtension
  extends ExtensionId[ConfigurationsExtensionImpl]
  with ExtensionIdProvider {

  override def lookup() = ConfigurationsExtension

  override def createExtension(system: ExtendedActorSystem) = new ConfigurationsExtensionImpl

  override def get(system: ActorSystem): ConfigurationsExtensionImpl = super.get(system)
}
