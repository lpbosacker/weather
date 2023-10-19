package weather

import requests._
import play.api.libs.json.*
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import Prefs._
import java.time.Instant
import StrUtil.stripUrl

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
  // ----------------------------------------------------------------------------------------
  def printObs(obs: Observation) : Unit = {
    println(s"---- Station ${obs.stationId} ----")
    println(s" Temperature : ${degCToF(obs.temperature)}")
    println(s"  Wind Speed : ${obs.windSpeed.getOrElse(0.0)}")
    println(s"    Wind Dir : ${obs.windDirection.getOrElse("None")}")
    println
  }
}
