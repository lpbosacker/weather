package weather

import requests._
import play.api.libs.json.*
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import Prefs._
import java.time.Instant
import StrUtil.stripUrl
import java.sql.PreparedStatement

case class Observation(
    stationId: String
  , timestamp: Instant
  , temperature: Float
  , dewPoint: Option[Float]
  , windDirection: Option[Int]
  , windSpeed: Option[Float]
  , windGust: Option[Float]
  , barometricPressure: Option[Int]
  , seaLevelPressure: Option[Int]
  , maxTemperatureLast24Hours: Option[Float]
  , minTemperatureLast24Hours: Option[Float]
  , precipitationLastHour: Option[Float]
  , precipitationLast3Hours: Option[Float]
  , precipitationLast6Hours: Option[Float]
  , relativeHumidity: Option[Float]
  , windChill: Option[Float]
  , heatIndex: Option[Float]
)

object Observation {
  def degCToF(degC: Float) : Float = ( 9.0 / 5.0 * degC + 32.0 ).toFloat
  def degCToF(degC: Option[Float]) : Float =
    if (degC.isDefined) degCToF(degC.get)
    else 0.0

  val convDtStr = DateTimeUtil.strToInstant _
  // ----------------------------------------------------------------------------------------
  implicit val observationReads: Reads[Observation] = (
    ((JsPath \ "properties" \ "station").read[String]).map(stripUrl) and
      ((JsPath \ "properties" \ "timestamp").read[String]).map(convDtStr) and
      (JsPath \ "properties" \ "temperature" \ "value").read[Float] and
      (JsPath \ "properties" \ "dewpoint" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "windDirection" \ "value").readNullable[Int] and
      (JsPath \ "properties" \ "windSpeed" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "windGust" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "barometricPressure" \ "value").readNullable[Int] and
      (JsPath \ "properties" \ "seaLevelPressure" \ "value").readNullable[Int] and
      (JsPath \ "properties" \ "maxTemperatureLast24Hours" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "minTemperatureLast24Hours" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "precipitationLastHour" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "precipitationLast3Hours" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "precipitationLast6Hours" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "relativeHumidity" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "windChill" \ "value").readNullable[Float] and
      (JsPath \ "properties" \ "heatIndex" \ "value").readNullable[Float]
    )(Observation.apply _)

  // ----------------------------------------------------------------------------------------
  // conversion from JsValue to Observation
  def toObservation(jsv: JsValue): Option[Observation] =
    jsv.validate[Observation](observationReads) match {
      case JsSuccess(obs, _) => {
        // println("successful parse")
        Some(obs)
      }
      case e: JsError => {
        println((jsv \ "properties" \ "@id").as[String])
        None
      }
    }
  // register implicit conversion function
  given Conversion[JsValue, Option[Observation]] = toObservation(_)

  // -------------------------------------------------------------------------
  def getLatestObservations(stationIds: List[String]) : List[Option[Observation]] =
    stationIds.map(stationId => getLatestObservation(stationId) )
  // -------------------------------------------------------------------------
  def getLatestObservation(stationId: String) : Option[Observation] = {
    val obsUrl = s"${Prefs.stationsUrl}/${stationId}/${Prefs.latestObservationSuffix}"
    try
      val resp = requests.get(obsUrl)
      if (resp.statusCode == 200)
        toObservation(Json.parse(resp.text()))
      else
        println(s"Failed response status = ${resp.statusCode}")
        None
    catch {
      case ex: requests.RequestFailedException => {
        println(s"Http request failed for ${stationId}")
        None
      }
      case _ => {
        println(s"Other error")
        None
      }
    }
  }
  // ----------------------------------------------------------------------------------------
  def insertObservations(obs: List[Observation]): Unit = {

    val insertObservationSql = (
      "INSERT INTO weather.observation " +
        "( station_id, timestamp, temperature, dew_point" +
        ", wind_direction, wind_speed, wind_gust, barometric_pressure" +
        ", sea_level_pressure, max_temp_last_24hr, min_temp_last_24hr" +
        ", precipitation_last_hr, precipitation_last_3hr" +
        ", precipitation_last_6hr, relative_humidity" +
        ", wind_chill, heat_index) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
        "ON CONFLICT ON CONSTRAINT observation_pk DO NOTHING"
      ).stripMargin

    // -------------------------------------------------------------------------
    def addObservation(stmt: java.sql.PreparedStatement, o: Observation): Unit =
      import java.sql.Types
      // for Option[String] -> if None then setNull VARCHAR else setString()
      def setOptionN[T](parameterindex: Int, optN: Option[T]): Unit =
        optN match {
          case Some(n) => stmt.setObject(parameterindex, n, Types.NUMERIC)
          case None => stmt.setNull(parameterindex, Types.NUMERIC)
        }
      // -------------------------------------------------------------------------
      stmt.setString(1, o.stationId)
      stmt.setTimestamp(2, DateTimeUtil.instantToTimestamp(o.timestamp))
      stmt.setObject(3, o.temperature, Types.NUMERIC)
      setOptionN(4, o.dewPoint)
      setOptionN(5, o.windDirection)
      setOptionN(6, o.windSpeed)
      setOptionN(7, o.windGust)
      setOptionN(8, o.barometricPressure)
      setOptionN(9, o.seaLevelPressure)
      setOptionN(10, o.maxTemperatureLast24Hours)
      setOptionN(11, o.minTemperatureLast24Hours)
      setOptionN(12, o.precipitationLastHour)
      setOptionN(13, o.precipitationLast3Hours)
      setOptionN(14, o.precipitationLast6Hours)
      setOptionN(15, o.relativeHumidity)
      setOptionN(16, o.windChill)
      setOptionN(17, o.heatIndex)
      stmt.addBatch()
    // ---------------------------------------------------------------------------
    DbUtil.insertList(insertObservationSql, addObservation: (PreparedStatement, Observation) => Unit, obs)
  }

  // ----------------------------------------------------------------------------------------
  def printObs(obs: Observation) : Unit = {
    println(s"---- Observation ${obs.stationId} at ${obs.timestamp} ----")
    println(s"        Temperature : ${degCToF(obs.temperature)}")
    println(s"          Dew point : ${degCToF(obs.dewPoint)}")
    println(s"           Wind Dir : ${obs.windDirection.getOrElse("None")} deg")
    println(s"         Wind Speed : ${obs.windSpeed.getOrElse("-")}")
    println(s"          Wind Gust : ${obs.windGust.getOrElse("-")}")
    println(s"Barometric Pressure : ${obs.windGust.getOrElse("-")}")
  }
}
