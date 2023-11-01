package weather

import java.time.{Instant, OffsetDateTime, ZoneOffset, LocalDateTime}
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal._
import java.util.TimeZone
import TsInterval._

object DateTimeUtil {

  given dtFormatter : DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  // ISO_OFFSET_DATE_TIME = 2023-10-19T13:03:16.167+00:00
  java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT"))

  val convDtStr = DateTimeUtil.strToInstant _
  def strToOffsetDateTime(isoStr: String)(using dtFormatter: DateTimeFormatter) : OffsetDateTime =
    OffsetDateTime.parse(isoStr, dtFormatter)

  def sqlTsToStr(sqlTs: java.sql.Timestamp) : String =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(sqlTs)

  def isoStrToSqlTs(isoStr: String) : java.sql.Timestamp =
    instantToTimestamp(strToInstant(isoStr))
    
  def toSqlDate(dt: OffsetDateTime) : java.sql.Date = java.sql.Date.valueOf(dt.toLocalDate())

  def strToInstant(s: String) : Instant = Instant.from(dtFormatter.parse(s))

  def instantToTimestamp(i: Instant): java.sql.Timestamp =
    Timestamp.valueOf(java.time.LocalDateTime.ofInstant(i, ZoneOffset.UTC))

  def offsetDtToTimestamp(dt: OffsetDateTime): java.sql.Timestamp =
    Timestamp.valueOf(dt.toLocalDateTime())

  def currentSqlTs() : java.sql.Timestamp = Timestamp(System.currentTimeMillis())

  def instantPlusHr(i: Instant, hrs : Int) : Instant = i.plus(hrs, ChronoUnit.HOURS)

  def sqlTsPlusHr(sqlTs: java.sql.Timestamp, hours: Int) : java.sql.Timestamp =
    DateTimeUtil.instantToTimestamp(DateTimeUtil.instantPlusHr(sqlTs.toInstant(),hours) )

  def sqlTsInterval(ts: java.sql.Timestamp, intervalHrs: Int) : TsInterval =
    if (intervalHrs > 0)
      TsInterval(ts, sqlTsPlusHr(ts,intervalHrs))
    else
      TsInterval(sqlTsPlusHr(ts,intervalHrs), ts)

  def last24Hours : TsInterval = sqlTsInterval(currentSqlTs(), -24)
}

