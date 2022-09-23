package domain

case class Log(ip: String, ident: String, user: String, datetime: String, status: String, size: String, referer: String, userAgent: String, unk: String, method: String, uri: String, http: String)

object Log {

    val default = Log("ip", "ident", "user", "datetime", "status", "size", "referer", "userAgent", "unk", "method", "uri", "http")
    def fromInt(i: Int) =  Log(s"ip_$i", "ident", "user", s"datetime_$i", "status", "size", "referer", "userAgent", "unk", "method", "uri", "http")
    
    val fields = Array("ip", "ident", "user", "datetime", "status", "size", "referer", "userAgent", "unk", "method", "uri", "http")

    def apply(s: Any): Log = Log("","","","","","","","","","", "", s.toString)

    def fromMap(map: Map[String, AnyRef]) = {
        Log.apply(
            map.get("ip").map(_.toString()).getOrElse(""), 
            map.get("ident").map(_.toString()).getOrElse(""), 
            map.get("user").map(_.toString()).getOrElse(""), 
            map.get("datetime").map(_.toString()).getOrElse(""), 
            map.get("status").map(_.toString()).getOrElse(""), 
            map.get("size").map(_.toString()).getOrElse(""), 
            map.get("referer").map(_.toString()).getOrElse(""), 
            map.get("userAgent").map(_.toString()).getOrElse(""), 
            map.get("unk").map(_.toString()).getOrElse(""), 
            map.get("method").map(_.toString()).getOrElse(""), 
            map.get("uri").map(_.toString()).getOrElse(""), 
            map.get("http").map(_.toString()).getOrElse("")
        )
    }
}