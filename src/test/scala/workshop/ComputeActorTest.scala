package workshop

import akka.actor._
import akka.testkit.{EventFilter, TestActorRef}
import scala.concurrent.duration._


class ComputeActorTest extends AkkaSpec {

  trait Actor {
    val computeActor = TestActorRef(ComputeActor.props(1 second))
  }

  it should "compute addition" in new Actor {
    computeActor ! Addition(9, 3)
    expectMsg(12)
  }

  it should "compute division" in new Actor {
    computeActor ! Division(9, 3)
    expectMsg(3)
  }

  it should "initially have zero completed tasks" in new Actor {
    computeActor ! GetNumCompletedTasks
    expectMsg(NumCompletedTasks(0))
  }

  it should "increment number of completed tasks" in new Actor {
    computeActor ! Addition(1, 1)
    expectMsgClass(classOf[Integer]) // Result from addition

    computeActor ! GetNumCompletedTasks
    expectMsg(NumCompletedTasks(1))

    computeActor ! Division(1, 1)
    expectMsgClass(classOf[Integer]) // Result from division

    computeActor ! GetNumCompletedTasks
    expectMsg(NumCompletedTasks(2))
  }

  it should "not increment number of completed tasks when division fails with arithmetic exception" in new Actor {
    // Prevents stack trace from displaying when running tests
    intercept[ArithmeticException] {
      computeActor.receive(Division(1, 0))
    }

    computeActor ! GetNumCompletedTasks
    expectMsg(NumCompletedTasks(0))
  }

  it should "log num completed tasks every configured interval on format 'Num completed tasks: <num_completed>'" in {

    TestActorRef(Props(new ComputeActor(100 millis)))

    // Throws timeout exception after 3 seconds if filter does not match
    EventFilter.info(start = "Num completed tasks", occurrences = 2).intercept( {

    })
  }
}