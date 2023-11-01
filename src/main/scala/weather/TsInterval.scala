package weather

import DateTimeUtil.sqlTsToStr

case class TsInterval(startTs: java.sql.Timestamp, endTs: java.sql.Timestamp) {
  override def toString() : String = s"start=${sqlTsToStr(startTs)}&end=${sqlTsToStr(endTs)}"
}

