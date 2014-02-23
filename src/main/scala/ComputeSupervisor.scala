package workshop

import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{OneForOneStrategy, ActorRef, Actor}
import akka.event.Logging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global


case class StartComputeActor(actorName: String)
case class Tick()

class ComputeSupervisor(computeActorFactory: ComputeActorFactory) extends Actor {

  val log = Logging(context.system, this)

  def this() = this(new ComputeActorFactory())

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute, loggingEnabled = false) {
      case _: ArithmeticException => {
        log.error("Resuming compute actor due to arithmetic exception")
        Resume
      }
      case e: Exception => {
        log.error("Restarting compute actor due to exception. Reason: {}", e.getMessage)
        Restart
      }
    }

  override def preStart() = scheduleTick()

  def receive = {
    case startComputeActor : StartComputeActor => {
      val computeActor: ActorRef = computeActorFactory.create(context, startComputeActor.actorName)
      sender ! computeActor
      log.info("Started compute actor with name {}", startComputeActor.actorName)
    }
    case Tick => {
      log.info("ticktick")
      scheduleTick()
    }
  }

  def scheduleTick() = {
    context.system.scheduler.scheduleOnce(1 second, self, Tick)
  }

}
