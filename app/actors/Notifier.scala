package actors

import akka.actor._
import models._
import play.api.libs.json.Json

/**
 * Created by shishir on 8/20/14.
 */
object Notifier {

  case class Connected(id: String, ref: ActorRef)
  case class Exited(id: String, ref: Seq[ActorRef])

  def props(out: ActorRef, sup: ActorRef, id: String) = Props(new Notifier(out, sup, id))

}

class Notifier(out: ActorRef, sup: ActorRef, id: String) extends Actor with ActorLogging {

  /*val myActor = context.actorOf(MyActor.props(self), "member-"+id)*/

  import Notifier._
  import Supervisor._

  def receive = {

    case Connected(id, ref) => {
      val connectedMsg: WsResponse = new WsResponse("Connected", id, Json.toJson("connected"))
      ref ! connectedMsg
    }

    case Exited(id, ref) => {
      val exitMsg: WsResponse = new WsResponse("Disconnected", id, Json.toJson("exited"))
      ref.foreach(_ ! exitMsg)
    }

    case di: SendMessage => {
      log.info("Reached SendMessage case in Notifier")
      val account: WsResponse = new WsResponse("Message", id, Json.toJson(di.message))
      sup ! SuperSend(di.to, account)
    }

    case lm: ListMemberIds => {
      val account: WsResponse = new WsResponse("List", "rew3", Json.obj("msg" -> membersMap.map(_._1).toSeq))
      out ! account
    }
  }

  override def preStart(): Unit = {
    sup ! InitConnection(id, out)
  }

  override def postStop(): Unit = {
      log.info("postStop reached")
      sup ! Disconnect(id)
    }
}