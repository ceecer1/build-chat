package actors

import akka.actor.{ActorLogging, Actor}
import actors.Supervisor.Connected

/**
 * Created by shishir on 8/24/14.
 */

object Supervisor {
  case class Connected(user: String)
}

class Supervisor extends Actor with ActorLogging {

  def receive = {
    case Connected(user) => {
      log.info(s"connected $user")
    }

  }

}
