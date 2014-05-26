package workshop

import scala.language.postfixOps
import akka.actor._
import work._
import scala.concurrent.duration._


class SuperComputeActorTest extends AkkaSpec {
  val timeout: FiniteDuration = 300 millis

  it should "compute risky work without failures" in  {
    suppressStackTraceNoise {
      val superComputeActor = system.actorOf(Props(classOf[SuperComputeActor]))

      superComputeActor ! RiskyAddition(1,3,0)
      expectMsg(RiskyAdditionResult(4))
    }
  }

  it should "compute risky work without failures in parallel" in  {
    suppressStackTraceNoise {
      val superComputeActor = system.actorOf(Props(classOf[SuperComputeActor]))

      val workList = List(RiskyAddition(1,3,150), RiskyAddition(2,3,150),  RiskyAddition(4,2,150))
      val workListResults = List(RiskyAdditionResult(4),RiskyAdditionResult(5),RiskyAdditionResult(6))

      workList.foreach(
        w => superComputeActor ! w
      )

      expectParallel(400) {
        val result1 = expectMsgClass(timeout, classOf[RiskyAdditionResult])
        val result2 = expectMsgClass(timeout, classOf[RiskyAdditionResult])
        val result3 = expectMsgClass(timeout, classOf[RiskyAdditionResult])
        List(result1, result2, result3).foreach(workListResults should contain (_))
      }
    }
  }

  it should "compute risky work with failures in parallel" in  {
    suppressStackTraceNoise {
      class WorkWithFailure extends RiskyWork {
        override def perform() = throw new RiskyWorkException("test exception")
      }
      val superComputeActor = system.actorOf(Props(classOf[SuperComputeActor]))

      val workList = List(RiskyAddition(1, 3, 150), new WorkWithFailure(),  RiskyAddition(4,2,150))
      val workListResults = List(RiskyAdditionResult(4),RiskyAdditionResult(6))

      workList.foreach(
        w => superComputeActor ! w
      )

      expectParallel(290) {
        val result1 = expectMsgClass(timeout, classOf[RiskyAdditionResult])
        val result2 = expectMsgClass(timeout, classOf[RiskyAdditionResult])
        List(result1, result2).foreach(workListResults should contain (_))
      }
    }
  }
}
