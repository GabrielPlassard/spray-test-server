import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._

import com.excilys.spraytestserver.Server

@RunWith(classOf[JUnitRunner])
class ServerSpec extends FlatSpec with Matchers with BeforeAndAfter{

  before {
    Server.start()
  }

  "With a server we" should  "launch the scenario" in {
      (1 + 1) should be (2)
  }

  after {
    Server.stop()
  }

}