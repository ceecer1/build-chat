package controllers

import actors.{Supervisor, Notifier}
import akka.actor.Props
import play.api.Play.current
import play.api.mvc._
import models.{WsResponse, ClientEvent, Contact}
import play.api.mvc.WebSocket.FrameFormatter
import play.libs.Akka
import scala.concurrent.Future

object Application extends Controller {

  implicit val inEventFrameFormatter = FrameFormatter.jsonFrame[ClientEvent]
  implicit val outEventFrameFormatter = FrameFormatter.jsonFrame[WsResponse]

  val mem = Map("k" -> "car")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def socket(id: String) = WebSocket.tryAcceptWithActor[ClientEvent, WsResponse] { request =>
    Future.successful(mem.get("k") match {
      case None => Left(Forbidden)
      case Some(_) => {
        val sup = Akka.system.actorOf(Props[Supervisor])
        Right(Notifier.props(_, sup, id))
      }
    })
  }

}