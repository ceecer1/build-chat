package actors

import akka.actor._
import models._
import play.api.libs.json.Json

/**
 * Created by shishir on 8/20/14.
 */
case class Member(id: String, val receiver: ActorRef, val sender: ActorRef)

object Notifier {

  case class DisconnectMember(ref: ActorRef)

  var members = Map.empty[String, Member]
  def props(out: ActorRef, sup: ActorRef, id: String) = Props(new Notifier(out, sup, id))

}

//TODO: http://stackoverflow.com/questions/26476827/private-communication-between-client-and-server-using-websockets-play-2-3-x

class Notifier(out: ActorRef, sup: ActorRef, id: String) extends Actor with ActorLogging {

  /*val myActor = context.actorOf(MyActor.props(self), "member-"+id)*/


  /*val c = (sendActor ? Init(id, receiveActor)).map{
    case c: ConnectedWS[_] =>
      play.Logger.info(s"Connected Member with ID:$id")
      members = members + (id -> Member(id, receiveActor, sendActor))
      c
  }*/

  import Notifier._
  import Supervisor._

  def receive = {
    /*case no: Connect => {
      if (!members.contains(id)) {
        log.info(id)
        members = members + (id -> Member(id, out, sender))
      }
      val account: WsResponse = new WsResponse("Connect", "note", Json.toJson("connected"))
      out ! account
    }*/
    case di: SendMessage => {
      log.info("Reached SendMessage case in Notifier")
      val account: WsResponse = new WsResponse("Message", id, Json.toJson(di.message))
      sup ! SuperSend(di.to, account)
    }

    case ro: Disconnect => {
      val account: WsResponse = new WsResponse("activity", "rough", Json.obj("msg" -> "disconnected"))
      members.get(id).foreach{ m =>
        members = members - id
        m.receiver ! PoisonPill
        m.sender ! PoisonPill
      }
    }
    case lm: ListMemberIds => {
      val account: WsResponse = new WsResponse("activity", "rough", Json.obj("msg" -> members.map(_._1).toSeq))
      membersMap.get(id).get ! account
    }
  }

  override def preStart(): Unit = {
    sup ! Connected(id, out)
  }

  override def postStop(): Unit = {
      log.info("postStop reached")
      sup ! Disconnect(id)
    }
}