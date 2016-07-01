package actor.circuitbreaker

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestActorRef
import org.mockito.Matchers.{anyString, refEq}
import org.mockito.Mockito.{times, verify}
import org.scalatest.concurrent.Eventually._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec}

class CircuitBreakerActorTest extends FlatSpec with BeforeAndAfterEach with BeforeAndAfterAll with MockitoSugar {

  var service: Service = _
  var circuitBreakerActor: ActorRef = _

  implicit val system = ActorSystem(getClass.getSimpleName)

  override protected def beforeEach() = {
    service = mock[Service]
    circuitBreakerActor = TestActorRef.apply(new CircuitBreakerActor(service.functionToCall))
  }


  override protected def afterAll() = {
    system.terminate()
  }


  "CBA" should "call passed function" in {

    circuitBreakerActor ! Message("1")
    eventually {
      verify(service).functionToCall(refEq("1"))
    }
  }

  it should "call service method twice" in {
    circuitBreakerActor ! Message("1")
    circuitBreakerActor ! Message("2")
    eventually {
      verify(service, times(2)).functionToCall(anyString())
    }
  }

  class Service {
    def functionToCall(s: String): String = {
      s
    }
  }

}
