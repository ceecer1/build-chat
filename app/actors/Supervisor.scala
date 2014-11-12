package actors

import actors.Notifier.Exited
import akka.actor._
import models.WsResponse
import play.api.libs.json.{JsObject, Json}
import play.libs.Akka

/**
 * Created by shishir on 11/6/14.
 */


object Supervisor {

  case class SuperSend(id: String, msg: WsResponse)
  case class InitConnection(id: String, ref: ActorRef)
  case class Disconnect(id: String)

  var membersMap = Map[String, ActorRef]()

  def props(name: String): Props = Props(classOf[Supervisor], name)

  //used for sending messages to actor
  val instance:ActorRef = Akka.system.actorOf(Supervisor.props("activisor"))

}

class Supervisor(name: String) extends Actor with ActorLogging {

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
      log.info(to)
      membersMap.get(to).get ! msg
    }

    case Disconnect(id) => {
      log.info(s"disconnecting member $id")
      membersMap.get(id).foreach{ m =>
        membersMap -= id
      }
      //sender ! Exited(id, membersMap.map(_._2).toSeq)
    }

    case Terminated(out) => log.info(s"The Actor Path ${out.path} terminated")

    }
}
