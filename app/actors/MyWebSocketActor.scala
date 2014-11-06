package actors

import akka.actor._
import models._
import actors.Supervisor.Connected
import play.api.libs.json.Json

/**
 * Created by shishir on 8/20/14.
 */
case class Member(id: String, val receiver: ActorRef, val sender: ActorRef)

object MyWebSocketActor {
  var members = Map.empty[String, Member]
  def props(out: ActorRef, sup: ActorRef, id: String) = Props(new MyWebSocketActor(out, sup, id))
}

class MyWebSocketActor(out: ActorRef, sup: ActorRef, id: String) extends Actor with ActorLogging {

  import MyWebSocketActor._
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
      /*log.info(id)*/
      members = members + (id -> Member(id, out, sender))
    }
    sup ! Connected(id)
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