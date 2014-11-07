package actors

import akka.actor._
import models.WsResponse
import play.api.libs.json.{JsObject, Json}

/**
 * Created by shishir on 11/6/14.
 */


object Supervisor {

  case class SuperSend(id: String, msg: WsResponse)
  case class Connected(id: String, ref: ActorRef)
  case class Disconnect(id: String)

  var membersMap = Map[String, ActorRef]()

}

class Supervisor extends Actor with ActorLogging {

  import Notifier._
  import Supervisor._

  def receive = {
    /*case Disconnect => {
      superVisor ! DisconnectMember(self)
      }*/

    case Connected(id, out) => {
      if (!membersMap.contains(id)) {
        membersMap += id -> out
        log.info(s"Connected id: $id")
      }
    }

    case SuperSend(to, msg) => {
      log.info("Reached SuperSend in Supervisor")
      membersMap.get(to).get ! msg
      if(membersMap.exists(_._1 == to)) {
        log.info(s"Member obtained ${membersMap.get(to).get}")
        val sender = context.actorOf(Sender.props(self), "member-" + to)
        sender ! msg
      }
    }

    case Disconnect(id) => {
      log.info("disconnecting member")
      membersMap.get(id).foreach{ m =>
        m ! PoisonPill
        membersMap -= id
      }
    }

    }
}
