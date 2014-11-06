package models

import play.api.libs.json.Json

case class Contact(name: Option[String],
                   roll: Option[String],
                   address: Option[String]
                    )

object Contact {
  implicit val format = Json.format[Contact]
}