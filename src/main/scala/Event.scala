/**
 * Created by Yaroslav on 09.07.15.
 */
class Event extends OneOutGoing{
  def getName(): String ={
    name
  }
  def getId() : String = {
    id
  }
  def getNextNode() : FlowNode = {
    nextNode
  }
}

class EndEvent extends Event {

}

class StartEvent extends Event{

}
