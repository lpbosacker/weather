package weather

import java.time.format.DateTimeFormatter
import com.typesafe.config.{Config, ConfigFactory}

object Prefs {
  println("Loading prefs")
  val appConfig = sys.env.get("WEATHER_CONFIG_PATH") match {
    case Some(path) => ConfigFactory.parseFile(new java.io.File(s"${path}/application.conf"))
    case None => ConfigFactory.load("application.conf")
  }
  val dbConfigName = appConfig.getString("dbConfigName")
  val stationsUrl = appConfig.getString("stationsURL")
  val latestObservationSuffix = appConfig.getString("latestObservationSuffix")
  val countyURL = appConfig.getString("countyURL")
  val fetchSize = appConfig.getInt("fetchSize")
  val insertBatchSize = appConfig.getInt("insertBatchSize")

  // given dtFormatter : DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

}
