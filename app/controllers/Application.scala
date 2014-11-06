package controllers

import play.api.Play.current
import play.api.mvc._
import models.{WsResponse, ClientEvent, Contact}
import play.api.mvc.WebSocket.FrameFormatter
import scala.concurrent.Future
import actors.{Supervisor, MyWebSocketActor}
import akka.actor.Props
import play.api.libs.concurrent.Akka

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
        Right(MyWebSocketActor.props(_, sup, id))
      }
    })
  }

}