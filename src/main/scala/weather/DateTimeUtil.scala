package weather

import java.time.{Instant, OffsetDateTime, ZoneOffset, LocalDateTime}
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import Prefs._

object DateTimeUtil {

  def strToOffsetDT(isoStr: String)(using dtFormatter: DateTimeFormatter) : OffsetDateTime =
    OffsetDateTime.parse(isoStr, dtFormatter)
    
  def toSqlDt(dt: OffsetDateTime) : java.sql.Date = java.sql.Date.valueOf(dt.toLocalDate())

  def strToInstant(s: String) : Instant = Instant.from(dtFormatter.parse(s))

  def instantToTimestamp(i: Instant): Timestamp = {
    Timestamp.valueOf(LocalDateTime.ofInstant(i, ZoneOffset.UTC))
  }

  def currentSqlTs() : Timestamp = Timestamp(System.currentTimeMillis())

}

