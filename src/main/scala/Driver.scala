package weather

import com.typesafe.config.{Config, ConfigFactory}
import weather.Station._
import weather.Observation._

import java.io.FileWriter
import java.sql.Connection
import scala.util.{Failure, Success, Try}
import sys._

object Main {
  // -----------------------------------------------------------------
  @main def run() : Unit = {

    println("Running!")

    // ---------------------------------------------------------------
    def someCount[T](optList: List[Option[T]]): Int =
      optList.foldLeft(0) { (nSome, opt) =>
        nSome + {
          if (opt.isDefined) 1 else 0
        }
      }
    // ---------------------------------------------------------------

    /* ============================================================== *
    println("Fetching counties from NWS")
    val counties = County.getNWSCounties
    println(s"Retrieved ${counties.size} counties")

    County.insertCounties(counties)

    println("Retrieving stations from web api")
    val stations = Station.getNWSStations
    println(s"\n retrieved ${stations.size} stations")

    println("Inserting stations into database")
    Station.insertStations(stations)
    println(" - stations inserted")

    val testStationNames = List(
            "KFCM"
          , "MAYM5"
          , "MN022"
          , "MN023"
          , "TS642"
        )

    val mnWhere = """station_id in (
          select station_id from weather.station join weather.county using (county_id)
          where state = 'MN' )""".stripMargin
     * ============================================================== */
    /* ============================================================== *
      println("Retrieving stations from database")
      val stations = Station.getDbStations()
      println(s" - retrieved ${stations.size} stations from database")
      val stationObs = stations.map(_.station_id).map(Observation.getLatestObservation(_))

      println(s"Found ${someCount(stationObs)} observations from stations ${stationObs.size}")
      Observation.insertObservations(stationObs.flatten)
     * ============================================================== */
    val flyingCloud = "KMSP"
    val fcObs = Observation.getStationObservations(flyingCloud, DateTimeUtil.last24Hours).flatten
    fcObs.foreach(Observation.printObs)
  }
}


