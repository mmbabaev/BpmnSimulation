/**
 * Created by Yaroslav on 09.07.15.
 */

class Task  extends OneOutGoing{
  //nextNode = null
  def getName(): String ={
    name
  }
  def getId() : String = {
    id
  }
  def getNextNode(): FlowNode ={
    nextNode
  }
}

class UserTask extends Task{

}

class ServiceTask extends Task{

}
