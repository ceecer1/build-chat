package actors

import akka.actor._
import models.WsResponse
import play.api.libs.json.{JsObject, Json}

/**
 * Created by shishir on 11/6/14.
 */


object MyActor {

  case class Send(msg: WsResponse)
  case object Disconnect

  def props(superVisor: ActorRef): Props = Props(new MyActor(superVisor))

}

class MyActor(superVisor: ActorRef) extends Actor with ActorLogging {

  import Notifier._
  import MyActor._

  def receive = {
    case Disconnect => {
      superVisor ! DisconnectMember(self)
      }

    case Send(msg) => {
      log.info("Received event at MyActor receive Send case")
      sender ! msg
    }
    }
}
