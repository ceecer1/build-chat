package models

import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.JsString

/**
 * Created by shishir on 8/23/14.
 */
sealed trait ClientEvent

case class Broadcast(from: String, payload: WsResponse) extends ClientEvent

object Broadcast {
  implicit val format = Json.format[Broadcast]
}

case class ListMemberIds(me: String) extends ClientEvent

object ListMemberIds {
  implicit val format = Json.format[ListMemberIds]
}

case class Connect(id: String = "connect") extends ClientEvent

object Connect {
  implicit val format = Json.format[Connect]
}

case class SendMessage(to: String, message: String) extends ClientEvent

object SendMessage {
  implicit val format = Json.format[SendMessage]
}

case class Disconnect(id: String) extends ClientEvent

object Disconnect {
  implicit val format = Json.format[Disconnect]
}

object ClientEvent {
  implicit def clientEventFormat: Format[ClientEvent] = Format(
    (__ \ "event").read[JsString].flatMap {
      case JsString("connect") => Connect.format.map(identity)
      case JsString("msg") => SendMessage.format.map(identity)
      case JsString("disconnect") => Disconnect.format.map(identity)
      case JsString("list") => ListMemberIds.format.map(identity)
      case JsString("broadcast") => Broadcast.format.map(identity)
      case other => Reads(_ => JsError("Unknown client event: " + other))
    },
    Writes {
      case no: Connect => Connect.format.writes(no)
      case br: Broadcast => Broadcast.format.writes(br)
      case di: SendMessage => SendMessage.format.writes(di)
      case ro: Disconnect => Disconnect.format.writes(ro)
      case lm: ListMemberIds => ListMemberIds.format.writes(lm)
    }
  )

  /**
   * Formats WebSocket frames to be ClientEvents.
   */
  implicit def clientEventFrameFormatter: FrameFormatter[ClientEvent] = FrameFormatter.jsonFrame.transform(
    clientEvent => Json.toJson(clientEvent),
    json => Json.fromJson[ClientEvent](json).fold(
      invalid => throw new RuntimeException("Bad client event on WebSocket: " + invalid),
      valid => valid
    )
  )

}

