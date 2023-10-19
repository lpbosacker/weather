package weather

import com.typesafe.config.ConfigFactory
case class DbConfig(configName: String) {

  println(s"Loading database ${configName} connection configuration")
  val dbCfg = {
    sys.env.get("DB_CONFIG_PATH") match {
      case Some(path) => ConfigFactory.parseFile(
        new java.io.File(s"${path}/database.conf")).getConfig(configName)
      case None => ConfigFactory.load("database.conf")
    }
  }
  val vendor = dbCfg.getString("vendor")
  val driver = dbCfg.getString("driver")
  val dbname = dbCfg.getString("dbname")
  val user = dbCfg.getString("user")
  val password = dbCfg.getString("password")
  val host = dbCfg.getString("host")
  val port = dbCfg.getInt("port")
  val url = s"jdbc:${vendor}://${host}:${port}/${dbname.toLowerCase}"
}
