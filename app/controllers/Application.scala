package controllers

import play.api.mvc._
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.default.BSONCollection
import uk.gov.hmrc.mongo.MongoConnector
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

object Application extends Controller {

  protected val databaseName = "reactive"

  protected val mongoUri: String = s"mongodb://127.0.0.1:27017/$databaseName"

  implicit val mongoConnectorForTest = new MongoConnector(mongoUri)

  implicit val mongo = mongoConnectorForTest.db

  def bsonCollection(name: String)(failoverStrategy: FailoverStrategy = mongoConnectorForTest.helper.db.failoverStrategy): BSONCollection = {
    mongoConnectorForTest.helper.db(name, failoverStrategy)
  }

  def index = Action {
    val repo = new SimpleTestRepository()
    val d = TestObject("Field", None, Some(List(NestedModel("Key", "Value"),NestedModel("Key2", "Value2"))))
//    repo.save(d)


    repo.insert(d).onComplete {
      case Failure(e) => throw e
      case Success(lastError) => {
        println("successfully inserted document with lastError = " + lastError)
      }
    }
    Ok(views.html.index("Your new application is ready."))
  }

}