package weather

import scala.annotation.tailrec
import java.sql.{ResultSet, PreparedStatement}
import requests._
import play.api.libs.json._
import scala.util.{Failure, Success, Try}
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import StrUtil._
import Prefs._

case class Station(
    station_id: String
  , station_name: String
  , county_id: Option[String]
  , lon: Double
  , lat: Double
  , elev: Double
  , timezone: String
)

// ---------------------------------------------------------------------------

object Station {
  // -------------------------------------------------------------------------
  def rowToStation(rs: java.sql.ResultSet): Station =
    Station(
      rs.getString("station_id")
      , rs.getString("station_name")
      , { // handle null county => convert to Option Some|None
        val county = rs.getString("county_id")
        if (rs.wasNull()) None
        else Some(county)
      }
      , rs.getDouble("lon")
      , rs.getDouble("lat")
      , rs.getDouble("elev")
      , rs.getString("timezone")
    )

  // -------------------------------------------------------------------------
  val converter: java.sql.ResultSet => Station = rowToStation
  // ---------------------------------------------------------------------------
  // ---- retrieve station objects from database -------------------------------
  def getDbStations(where: Option[String] = None) : List[Station] =

    val dbc = DbUtil.getConnection(Prefs.dbConfigName)
    // val queryText =  "select * from weather.station" +
    val queryText =  "select * from weather.station" +
      {
      if (where.isDefined) s" where ${where.get}"
      else " ;"
      }

    println(s"SQL> ${queryText}")
    val select = dbc.prepareStatement(queryText)
    val rs = select.executeQuery()

    val stations = DbUtil.getResults(List[Station](), rs, converter)
    select.close()
    dbc.close()
    stations
  // ---------------------------------------------------------------------------

  // ---------------------------------------------------------------------------
  // ---- retrieve station objects from NWS api request ------------------------
  def getNWSStations: List[Station] = {

    val url= Prefs.stationsUrl
    val fetchSize = Prefs.fetchSize
    // -------------------------------------------------------------------------
    implicit val stationReads: Reads[Station] = (
      ((JsPath \ "properties" \ "stationIdentifier").read[String]).map(stripUrl) and
        ((JsPath \ "properties" \ "name").read[String]) and
        ((JsPath \ "properties" \ "county").readNullable[String]).map(stripOptUrl) and
        ((JsPath \ "geometry" \ "coordinates").read[Array[Double]]).map(_(0)) and
        ((JsPath \ "geometry" \ "coordinates").read[Array[Double]]).map(_(1)) and
        ((JsPath \ "properties" \ "elevation" \ "value").read[Double]) and
        ((JsPath \ "properties" \ "timeZone").read[String])
      )(Station.apply _)
    // -------------------------------------------------------------------------
    def toStation(jsv: JsValue): Option[Station] =
      jsv.validate[Station](stationReads) match {
        case JsSuccess(s, _) => Some(s)
        case e: JsError => {
          println((jsv \ "properties" \ "@id").as[String])
          None
        }
      }
    // -------------------------------------------------------------------------
    given Conversion[JsValue, Option[Station]] = toStation(_)
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    @tailrec def fetchStations(url: String, stations: List[Station]): List[Station] =
      val resp = requests.get(url, readTimeout = 5000)
      if (resp.statusCode != 200)
        stations
      else
        val respData = Json.parse(resp.text())
        val next = (respData \\ "pagination").head("next").as[String]
        val stationsJson = (respData \ "features").as[List[JsValue]]
        val newStations = stationsJson.map(toStation)
        /* -------------- attempt to track stations not parsed successfully ------ *
        val noneStations = newStations.foldLeft(0){
          (count, o) => count + { if (o.isDefined) 0 else 1}
          }
         if (noneStations > 0) println(s"${noneStations} skipped")
         * -------------- attempt to track stations not parsed successfully ------ */
        print(".")
        if (newStations.length < fetchSize)
          println("!")
          newStations.flatten ++ stations
        else fetchStations(next, newStations.flatten ++ stations)
    // -------------------------------------------------------------------------
    println(s"Requesting stations from ${url}")
    val stations = List[Station]()
    fetchStations(url, stations)
  }
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  def insertStations(stations : List[Station]) : Unit =
    val insertStationSql = (
      "INSERT INTO weather.station " +
      "( station_id, station_name, county_id, lon, lat, elev, timezone) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (station_id) DO NOTHING"
      )
    val currentTs = DateTimeUtil.currentSqlTs()
    // -------------------------------------------------------------------------
    def addStation(stmt: java.sql.PreparedStatement, s: Station) : Unit =
      stmt.setString(1, s.station_id)
      stmt.setString(2, s.station_name)
      { // county_id is Option[String] => map it to varchar or null
        s.county_id match {
          case Some(cn) => stmt.setString(3,cn)
          case None => stmt.setNull(3, java.sql.Types.VARCHAR)
        }
      }
      stmt.setDouble(4, s.lon)
      stmt.setDouble(5, s.lat)
      stmt.setDouble(6, s.elev)
      stmt.setString(7, s.timezone)
      stmt.addBatch()
    // ---------------------------------------------------------------------------
    DbUtil.insertList(insertStationSql, addStation: (PreparedStatement, Station) => Unit, stations)
}
