/*
package actors

import _root_.core.actors.Broadcast
import akka.actor.Actor.Receive
import models.{WsResponse, Broadcast}
import play.api.libs.json.{Json, JsObject}

/**
 * Created by shishir on 11/6/14.
 */

object Notification {

  class NotificationSupervisor extends Supervisor {

    def customBroadcast: Receive = {
      case Broadcast(from, res: WsResponse) =>
        // adds members to all messages
        val ids = Json.obj("members" -> Supervisor.members.map(_._1))

        Supervisor.members.foreach {
          case (id, member) =>
            member.sender ! Broadcast(from, res ++ ids)

          case _ => ()
        }
    }

  }

}
*/
