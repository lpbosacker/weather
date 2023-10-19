package weather

import java.sql.{Connection, DriverManager, PreparedStatement, Timestamp}
import com.typesafe.config.{Config, ConfigFactory}

import java.io.File
import scala.util.{Failure, Success, Try}
import scala.annotation.tailrec

object DbUtil {
  // -----------------------------------------------------------------
  // -----------------------------------------------------------------
  def getConnection(dbConfigName: String) : java.sql.Connection = {
    val cfg = DbConfig(dbConfigName)
    Class.forName(cfg.driver)
    val connection = Try(DriverManager.getConnection(cfg.url, cfg.user, cfg.password))
    connection match {
      case Success(c) => 
        c.setAutoCommit(false)
        println(s"Connected to ${cfg.dbname} as user ${cfg.user}")
        c
      case Failure(e) =>
        println(s"Error connecting to ${cfg.dbname}\n${e}")
        throw e
    }
  }
  // -----------------------------------------------------------------
  @tailrec def getResults[T](rlist: List[T], rs: java.sql.ResultSet
   , convFn: java.sql.ResultSet => T): List[T] =
    if (!rs.next())
      rlist
    else
      val s = convFn(rs)
      getResults(s +: rlist, rs, convFn)
  // -----------------------------------------------------------------
  def insertList[T](insertSql: String, addToBatch : (PreparedStatement, T) => Unit, rows : List[T]) : Unit =

    val insertBatchSize = Prefs.insertBatchSize
    // println(s"${rows.size} rows to insert : batch size = ${insertBatchSize}")
    // -----------------------------------------------------------------
    @tailrec def insertBatch[T](dbc: Connection, stmt: PreparedStatement, bindFn : T => Unit, rows: List[T]) : Unit =
      rows.take(insertBatchSize).foreach(r => bindFn(r))
      val inserts = stmt.executeBatch()
      val nInserts = inserts.reduce(_ + _)
      dbc.commit()
      val remaining = rows.drop(insertBatchSize)
      if (remaining.size > 0)
        insertBatch(dbc, stmt, bindFn, remaining)
    // -----------------------------------------------------------------

    if (rows.size > 0)
      val dbc = getConnection(Prefs.dbConfigName)
      val insertStmt = dbc.prepareStatement(insertSql)
      val bindFn = addToBatch(insertStmt, _: T)
      insertBatch(dbc, insertStmt, bindFn, rows)
    else
      println("No rows to insert")
  // ---------------- End of insertList[T] --------------------------
  def truncateTable(dbc: Connection, tableName: String) : Unit = {
    val truncateStmt = dbc.prepareStatement(s"TRUNCATE TABLE ONLY ${tableName}")
    val rstatus = truncateStmt.execute()
    dbc.commit()
  }
}
