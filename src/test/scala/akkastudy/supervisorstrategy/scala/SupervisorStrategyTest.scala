package akkastudy.supervisorstrategy.scala

import org.scalatest.FlatSpec
import akka.actor.{ActorRef, Props, ActorSystem}
import akka.util.Timeout
import akka.pattern.ask
import org.scalatest.matchers.MustMatchers
import scala.concurrent.duration._


class SupervisorStrategyTest extends FlatSpec with MustMatchers {
  behavior of "A supervisor strategy test"

  it should "Create any child actor and throw an Illegal Argument Exception" in {
    val system = ActorSystem("MySystem")

    import system.dispatcher
    implicit val timeout = new Timeout(5 seconds)

    val grandparent = system.actorOf(Props[OneForOneGrandparentActor], name = "GrandparentActorScala")

    val childActorFuture1 = grandparent ? Props[ExceptionalChildActor]

    grandparent ! Props[ExceptionalChildActor]
    grandparent ! Props[ExceptionalChildActor]

    Thread.sleep(3000)
    childActorFuture1 foreach {
      _.asInstanceOf[ActorRef] ! "IllegalArgumentException" //Asynchronous wait for the ref, throw the IAE
    }

    Thread.sleep(5000)
  }

  it should
    """Create any child actor and throw a NullPointerException then sends another
      | message since the actor, has been restarted""" in {

    val system = ActorSystem("MySystem")

    import system.dispatcher
    implicit val timeout = new Timeout(5 seconds)

    val grandparent = system.actorOf(Props[OneForOneGrandparentActor], name = "GrandparentActorScala")

    val childActorFuture1 = grandparent ? Props[ExceptionalChildActor]

    grandparent ! Props[ExceptionalChildActor]
    grandparent ! Props[ExceptionalChildActor]

    Thread.sleep(3000)
    childActorFuture1 foreach { a =>
      val actorRef = a.asInstanceOf[ActorRef]
        actorRef ! "NullPointerException" //Asynchronous wait for the ref, throw the IAE
        (actorRef ? "OK").foreach(_ must be("Message Received: OK"))
    }

    Thread.sleep(5000)
  }


  it should
    """Resume the child actor if an arithmetic exception is thrown as
      |  specified in the strategy at the parent""".stripMargin('|') in {

    val system = ActorSystem("MySystem")

    import system.dispatcher
    implicit val timeout = new Timeout(5 seconds)

    val grandparentRef = system.actorOf(Props[OneForOneGrandparentActor], name = "OneForOneGrandparentActorScala")

    grandparentRef ! Props[ExceptionalChildActor]
    grandparentRef ! Props[ExceptionalChildActor]
    grandparentRef ! Props[ExceptionalChildActor]

    Thread.sleep(3000)

    grandparentRef ? Props[ExceptionalChildActor] foreach { c =>
      val childActorRef = c.asInstanceOf[ActorRef]
        childActorRef ! "ArithmeticException"
    }

    Thread.sleep(5000)
  }

  it should "throw restart the child actor, not because of the parent but because of the grandparent" in {
    val system = ActorSystem("MySystem")

    import system.dispatcher
    implicit val timeout = new Timeout(5 seconds)

    val grandparent = system.actorOf(Props[OneForOneGrandparentActor], name = "GrandparentActorScala")

    grandparent ! Props[ExceptionalChildActor]
    grandparent ! Props[ExceptionalChildActor]
    grandparent ! Props[ExceptionalChildActor]

    val parentActorFuture = grandparent ? Props[OneForOneParentActor]

    Thread.sleep(3000)
    parentActorFuture foreach { a =>
      val parentActorFutureRef = a.asInstanceOf[ActorRef]
        val childActorFutureRef = parentActorFutureRef ? Props[ExceptionalChildActor]
        childActorFutureRef foreach {
          c =>
            val childActorRef = c.asInstanceOf[ActorRef]
            childActorRef ! "NullPointerException"
            childActorRef ? "OK" foreach (o => o must be("Message Received: OK"))
        }
    }

    Thread.sleep(5000)
  }

  it should
    """Using a One for One Grandparent Actor, and an All for One Parent,
      | all the parent's will share the same fate""".stripMargin('|') in {
    val system = ActorSystem("MySystem")

    import system.dispatcher
    implicit val timeout = new Timeout(5 seconds)

    val grandparent = system.actorOf(Props[OneForOneGrandparentActor], name = "GrandparentActorScala")

    grandparent ! Props[ExceptionalChildActor]
    grandparent ! Props[ExceptionalChildActor]
    grandparent ! Props[ExceptionalChildActor]

    val parentActorFuture = grandparent ? Props[AllForOneParentActor]

    Thread.sleep(3000)
    parentActorFuture foreach { a =>
      val parentActorFutureRef = a.asInstanceOf[ActorRef]
        val childActorFutureRef = parentActorFutureRef ? Props[ExceptionalChildActor]
        childActorFutureRef foreach { c =>
          val childActorRef = c.asInstanceOf[ActorRef]
            childActorRef ! "NullPointerException"
            childActorRef ? "OK" foreach (o => o must be("Message Received: OK"))
        }
    }
    Thread.sleep(5000)
  }
}