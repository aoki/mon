package controllers

import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError
import uk.gov.hmrc.mongo.json.{ReactiveMongoFormats, TupleFormats}
import uk.gov.hmrc.mongo.{CreationAndLastModifiedDetail, MongoConnector, ReactiveRepository}

import scala.concurrent.{Future, ExecutionContext}

case class NestedModel(a: String, b: String)

case class TestObject(aField: String,
                    anotherField: Option[String] = None,
                    optionalCollection: Option[List[NestedModel]] = None,
                    nestedMapOfCollections: Map[String, List[Map[String, Seq[NestedModel]]]] = Map.empty,
                    modifiedDetails: CreationAndLastModifiedDetail = CreationAndLastModifiedDetail(),
                    jsValue: Option[JsValue] = None,
                    location: Tuple2[Double, Double] = (0.0, 0.0),
                    id: BSONObjectID = BSONObjectID.generate) {

  def markUpdated(implicit updatedTime: DateTime) = copy(
    modifiedDetails = modifiedDetails.updated(updatedTime)
  )

}

object TestObject {

  import ReactiveMongoFormats.{objectIdFormats, localDateFormats, mongoEntity}

  implicit val formats = mongoEntity {

    implicit val locationFormat = TupleFormats.tuple2Format[Double, Double]

    implicit val nestedModelformats = Json.format[NestedModel]

    Json.format[TestObject]
  }
}

class SimpleTestRepository(implicit mc: MongoConnector)
  extends ReactiveRepository[TestObject, BSONObjectID]("simpleTestRepository", mc.db, TestObject.formats, ReactiveMongoFormats.objectIdFormats) {

  override def save(entity: TestObject)(implicit ec: ExecutionContext): Future[LastError] = {
    println(entity)
    super.save(entity)
  }

  override def ensureIndexes(implicit ec: ExecutionContext) = {
    val index1 = collection.indexesManager.ensure(Index(Seq("aField" -> IndexType.Ascending), name = Some("aFieldUniqueIdx"), unique = true, sparse = true))
    val index2 = collection.indexesManager.ensure(Index(Seq("anotherField" -> IndexType.Ascending), name = Some("anotherFieldIndex")))

    Future.sequence(Seq(index1, index2))
  }
}