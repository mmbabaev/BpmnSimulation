
/**
 * Created by Yaroslav on 09.07.15.
 */
class Flow{
  var id: String = null
  var name: String= null
  def getName(): String ={
    name
  }
  def getId() : String = {
    id
  }
  def getTarget(): FlowNode = {
    target
  }
  var target: FlowNode = null
  var source: FlowNode = null

}

class SequenceFlow extends Flow{
  var condition: String = null
}
