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

    println("Fetching counties from NWS")
    val counties = County.getNWSCounties
    println(s"Retrieved ${counties.size} counties")

    /* ============================================================== *
    County.insertCounties(counties)

    println("Retrieving stations from web api")
    val stations = Station.getNWSStations
    println(s"\n retrieved ${stations.size} stations")

    println("Inserting stations into database")
    Station.insertStations(stations)
    println(" - stations inserted")

    val carverStationNames = List(
        "KFCM"
      , "C9784"
      , "COOPMPXM5"
      , "E2706"
      , "F2164"
      , "F9274"
      , "HPN03" // what happened here?  save response to file and parse : No precip last 6 hours
      , "JDNM5"
      , "MAYM5"
      , "MN022"
      , "MN023"
      , "TS642"
    )
    val stationObs = Observation.getLatestObservations(carverStationNames)
    val goodCount = stationObs.foldLeft(0){ (ocount, obs) => ocount + { if (obs.isDefined) 1 else 0} }
    println(s"Found ${goodCount} valid")
    stationObs.flatten.map(obs => Observation.printObs(obs))
     * ============================================================== */
    /* ============================================================== *
    // retrieve existing stations from db
    println("Retrieving stations from database")
    val dbStations = Station.getDbStations()
    println(s" - retrieved ${dbStations.size} stations from database")
     * ============================================================== */
  }
}


