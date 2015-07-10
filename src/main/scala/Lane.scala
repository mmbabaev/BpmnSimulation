import scala.collection.mutable.ArrayBuffer

/**
 * Created by Yaroslav on 09.07.15.
 */
class Lane {
  var name: String = null
  var id: String =  null
  val flowNodes = new ArrayBuffer[FlowNode]
}

class LaneSet{
  val lanes = new ArrayBuffer[Lane]
}
