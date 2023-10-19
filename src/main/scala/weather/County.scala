package weather

import java.sql.Connection
import java.time.Instant
import play.api.libs.json.Reads.*
import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsValue, Json, Reads}
import DateTimeUtil._
import weather.Prefs._

import scala.util.{Failure, Success, Try}

case class County(
    countyId: String
  , countyName: String
  , state: String
)

object County {
  // ------------------------ database functions ------------------------------------------
  def insertCounties(counties: List[County]): Unit =
    val insertSql = (
      "INSERT INTO weather.county " +
        "( county_id, county_name, state) " +
        "VALUES (?, ?, ?) ON CONFLICT (county_id) DO NOTHING"
      )

    val insertBatchSize = Prefs.insertBatchSize
    // -------------------------------------------------------------------------
    def addToBatch(stmt: java.sql.PreparedStatement, c: County): Unit =
      stmt.setString(1, c.countyId)
      stmt.setString(2, c.countyName)
      stmt.setString(3, c.state)
      // stmt.setTimestamp(4, DateTimeUtil.instantToTimestamp(c.effectiveDate))
      stmt.addBatch()

    // ---------------------------------------------------------------------------
    def insertCounties(dbc: Connection, counties: List[County]): Unit =
      print(s"${counties.size} counties to insert")
      val insert = dbc.prepareStatement(insertSql)
      counties.take(insertBatchSize).foreach(s => addToBatch(insert, s))
      val inserts = insert.executeBatch()
      val nInserts = inserts.reduce(_ + _)
      dbc.commit
      println(s": inserted ${nInserts} counties")
      if (inserts.size == insertBatchSize)
        insertCounties(dbc, counties.drop(insertBatchSize))

    // -------------------------------------------------------------------------
    val dbc = DbUtil.getConnection(Prefs.dbConfigName)
    if (counties.size > 0)
      insertCounties(dbc, counties)
    else
      println("No counties to insert")
  // ---------- end insertCounties() ---------------------------------------

  // ----------------------------- web functions ------------------------------------------

  val stripUrl = StrUtil.stripUrl _
  val convDtStr = DateTimeUtil.strToInstant _

  implicit val countyReads: Reads[County] = (
    ((JsPath \ "properties" \ "id").read[String]).map(stripUrl) and
    ((JsPath \ "properties" \ "name").read[String]) and
    (JsPath \ "properties" \ "state").read[String]
    )(County.apply _)

  // ---- retrieve county objects from NWS api request ------------------------
  def getNWSCounties: List[County] = {

    val url = Prefs.countyURL
    // --------------- JsValue to Option[County] conversion ----------
    def toCounty(jsv: JsValue): Option[County] =
      jsv.validate[County](countyReads) match {
        case JsSuccess(c, _) => Some(c)
        case e: JsError => None
      }
    // ----------- define implicit conversion function ---------------
    given Conversion[JsValue, Option[County]] = toCounty(_)
    // -----------------------------------------------------------------

    val resp = requests.get(url)
    val countiesJson = (Json.parse(resp.text()) \ "features").as[List[JsValue]]
    countiesJson.flatMap(toCounty)
  }

}