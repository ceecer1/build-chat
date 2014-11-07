package actors

import actors.Notifier.Exited
import akka.actor._
import models.WsResponse
import play.api.libs.json.{JsObject, Json}

/**
 * Created by shishir on 11/6/14.
 */


object Supervisor {

  case class SuperSend(id: String, msg: WsResponse)
  case class InitConnection(id: String, ref: ActorRef)
  case class Disconnect(id: String)

  var membersMap = Map[String, ActorRef]()

}

class Supervisor extends Actor with ActorLogging {

  import Notifier.Connected
  import Supervisor._

  def receive = {

    case InitConnection(id, out) => {
      context.watch(out)
      if (!membersMap.contains(id)) {
        membersMap.map(m => sender ! Connected(id, m._2))
        membersMap += id -> out
      }
    }

    case SuperSend(to, msg) => {
      membersMap.get(to).get ! msg
    }

    case Disconnect(id) => {
      log.info(s"disconnecting member $id")
      val exitMsg: WsResponse = new WsResponse("Disconnected", id, Json.toJson("exited"))
      membersMap.get(id).foreach{ m =>
        membersMap -= id
      }
      membersMap.map(m => m._2 ! exitMsg)
    }

    case Terminated(out) => log.info(s"The Actor Path ${out.path} terminated")

    }
}
