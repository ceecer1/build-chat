package actors

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import models.WsResponse
import play.api.libs.json.Json

/**
 * Created by shishir on 11/7/14.
 */

object Sender {

  case class Send(sendTo: ActorRef, response: WsResponse)

  def props(superVisor: ActorRef): Props = Props(new Sender(superVisor))

}

class Sender(superVisor: ActorRef) extends Actor with ActorLogging {

  import Sender._

  def receive = {
    case Send(sendTo, response) => {
      //val account: WsResponse = new WsResponse("Message", id, Json.toJson(di.message))
      log.info("Sending response")
      sendTo ! response
    }
  }

}
