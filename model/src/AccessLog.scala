package model 

case class Log(ip: String, ident: String, user: String, datetime: String, status: String, size: String, referer: String, userAgent: String, unk: String, method: String, uri: String, http: String)

object Log {

    val fields = Array("ip", "ident", "user", "datetime", "status", "size", "referer", "userAgent", "unk", "method", "uri", "http")

    def fromMap(map: Map[String, AnyRef]) = {
        Log.apply(
            map("ip").toString(), 
            map("ident").toString(), 
            map("user").toString(), 
            map("datetime").toString(), 
            map("status").toString(), 
            map("size").toString(), 
            map("referer").toString(), 
            map("userAgent").toString(), 
            map("unk").toString(), 
            map("method").toString(), 
            map("uri").toString(), 
            map("http").toString())
    }
}