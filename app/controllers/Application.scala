package controllers

import actors.Supervisor.SuperSend
import actors.{Supervisor, Notifier}
import akka.actor.PoisonPill
import play.api.Play.current
import play.api.libs.json.Json
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
    val connectedMsg: WsResponse = new WsResponse("sent to actor", "ccer", Json.toJson("sent"))
    val ssend = new SuperSend("ccer", connectedMsg)
    Supervisor.instance ! ssend
    Ok(views.html.index("Your new application is ready."))
  }

  def shut = Action {
    Supervisor.instance ! PoisonPill
    Ok(views.html.index("Your new application is shut."))
  }

  def open = Action {
    val connectedMsg: WsResponse = new WsResponse("sent to new actor", "ccer", Json.toJson("new"))
    val ssend = new SuperSend("ccer", connectedMsg)
    Supervisor.instance ! ssend
    Ok(views.html.index("Your new application is open."))
  }

  def socket(id: String) = WebSocket.tryAcceptWithActor[ClientEvent, WsResponse] { request =>
    Future.successful(mem.get("k") match {
      case None => Left(Forbidden)
      case Some(_) => {
        val sup = Akka.system.actorOf(Supervisor.props("notifisor"))
        Right(Notifier.props(_, sup, id))
      }
    })
  }

}