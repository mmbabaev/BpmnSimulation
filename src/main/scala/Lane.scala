import scala.collection.mutable.ArrayBuffer

/**
 * Created by Yaroslav on 09.07.15.
 */
class Lane {
  var name: String = null
  var id: String =  null
  val flowNodes = new ArrayBuffer[FlowNode]

  def getName = name
  def getId = id
}

class LaneSet{
  val lanes = new ArrayBuffer[Lane]
  val laneNames = new ArrayBuffer[String]
  val laneIds = new ArrayBuffer[String]
}
