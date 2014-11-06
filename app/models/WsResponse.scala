package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.JsObject
import com.fasterxml.jackson.annotation.JsonValue

case class WsResponse(kind: String,
                   from: String,
                   message: JsValue
                    )


object WsResponse {
  implicit val format = Json.format[WsResponse]
}

