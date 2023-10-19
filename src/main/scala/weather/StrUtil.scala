package weather

object StrUtil {

  // strip url for all char up to last '/'
  def stripUrl(urlStr: String): String =
    urlStr.replaceFirst("""https://.*/""", "")
  // -------------------------------------------------------------------------
  // input Option[String] : if defined then strip url for all char up to last '/'
  val stripOptUrl: Option[String] => Option[String] = {
    _ match {
      case Some(url) => Some(url.replaceFirst("""https://.*/""", ""))
      case None => None
    }
  }
}
