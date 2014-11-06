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
  def props(out: ActorRef, id: String) = Props(new Notifier(out, id))

}

//TODO: http://stackoverflow.com/questions/26476827/private-communication-between-client-and-server-using-websockets-play-2-3-x

class Notifier(out: ActorRef, id: String) extends Actor with ActorLogging {

  val myActor = context.actorOf(MyActor.props(self), "member-"+id)


  /*val c = (sendActor ? Init(id, receiveActor)).map{
    case c: ConnectedWS[_] =>
      play.Logger.info(s"Connected Member with ID:$id")
      members = members + (id -> Member(id, receiveActor, sendActor))
      c
  }*/

  import Notifier._
  import MyActor._

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
      val account: WsResponse = new WsResponse("Message", id, Json.toJson(di.message))
      myActor ! Send(account)
      log.info(s"Sender $id")
      if(members.exists(_._1 == di.to))
        members.get(di.to).get.receiver ! account
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
      members.get(id).get.receiver ! account
    }
  }

  override def preStart(): Unit = {
    if (!members.contains(id)) {
      log.info(s"Adding $id")
      members = members + (id -> Member(id, out, sender))
    }
    //sup ! Connected(id)
    /*context.watch()*/
  }

  override def postStop(): Unit = {
    members.get(id).foreach{ m =>
      log.info(s"Removing $id")
      members = members - id
      m.receiver ! PoisonPill
      m.sender ! PoisonPill
    }
  }
}