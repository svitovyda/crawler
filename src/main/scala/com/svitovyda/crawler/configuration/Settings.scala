package com.svitovyda.crawler.configuration

import com.typesafe.config.Config


case class Settings(config: Config) {
  val maxPagesToStore: Int = config.getInt("crawler.settings.max-pages-to-store")
  val maxDepth: Int = config.getInt("crawler.settings.max-depth")
}

object Settings {
  implicit def conf2Settings: (Config) => Settings = Settings apply
}
