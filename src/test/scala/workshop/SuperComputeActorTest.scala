package workshop

import akka.actor._
import work._
import scala.concurrent.duration._
import scala.collection.immutable.Seq

class SuperComputeActorTest extends AkkaSpec {

  val timeout: FiniteDuration = 150 millis

  it should "compute one risky work in which has no failures" in  {
    val superComputeActor = system.actorOf(Props(classOf[SuperComputeActor]))

    superComputeActor ! RiskyAddition(1,3,0)

    expectMsg(timeout, RiskyAdditionResult(4))
  }

  it should "compute risky work in parallell when work has no failures" in  {
    val superComputeActor = system.actorOf(Props(classOf[SuperComputeActor]))

    val workList = List(RiskyAddition(1, 3, 100), RiskyAddition(2,3,100),  RiskyAddition(4,2,100))
    val workListResults = List(4,5,6)

    workList.foreach(
      w => superComputeActor ! w
    )

    val sequenceResult: Seq[RiskyAdditionResult] = expectMsgAllClassOf(timeout, classOf[RiskyAdditionResult])

    sequenceResult.seq.foreach(workListResults should contain (_))
  }

  it should "compute risky work in parallell when one has failuree" in  {
    class WorkWithFailure extends RiskyWork {
      override def perform() = throw new RiskyWorkException("test exception")
    }
    val superComputeActor = system.actorOf(Props(classOf[SuperComputeActor]))

    val workList = List(RiskyAddition(1, 3, 100), new WorkWithFailure(),  RiskyAddition(4,2,100))
    val workListResults = List(4,6)

    workList.foreach(
      w => superComputeActor ! w
    )

    val sequenceResult: Seq[RiskyAdditionResult] = expectMsgAllClassOf(timeout, classOf[RiskyAdditionResult])

    sequenceResult.seq.foreach(workListResults should contain (_))
  }

}
