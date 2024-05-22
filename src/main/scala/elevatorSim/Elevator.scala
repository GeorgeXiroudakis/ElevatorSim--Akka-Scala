package elevatorSim

import akka.actor._
import scala.concurrent.duration._


enum DIRECTION:
  case UP, DOWN


case class UpdateDirectionAndDestinationFloor()

case class ButtonPressed(userID: Int, floor: Int)

case class CanselRequest(userID: Int)

case class ReachNextFloor()

class Elevator(val ID: Int, NumofFloors: Int) extends Actor{
  import context.dispatcher

  private val elevatorSpeedPerFloor: Int = 1
  private val elevatorPickupDropofSpeed: Int = 1

  var needsToMove: Boolean = false
  var currentFloor = 0
  var destinationFloor = 0
  var direction: DIRECTION = DIRECTION.UP
  var requsts: Map[Int, Int] = Map() //maps the user ID with the floor he wants to go


  def receive: Receive = {


    //Precondition users must not be empty
    case UpdateDirectionAndDestinationFloor() =>
      if (requsts.nonEmpty) {
        val maxRequestedFloor = requsts.values.max
        val minRequestedFloor = requsts.values.min

        if(currentFloor < minRequestedFloor) direction = DIRECTION.UP
        else if(currentFloor > maxRequestedFloor) direction = DIRECTION.DOWN

        if(direction == DIRECTION.UP) destinationFloor = Math.min(maxRequestedFloor, NumofFloors)
        else destinationFloor =  Math.max(minRequestedFloor, 0)

      }else{
        needsToMove = false
      }



    //precondition that the floor is in bouts(this is usered for both callint the elevator and choising floor)
    case ButtonPressed(userID: Int, floor: Int) =>
        requsts += (userID -> floor)
        var size = requsts.size

        //if we were stoped because we had no users (CanselRequest) then we start moving so we reachnextfloor in elevatorSpeedPerFloor time
        if(!needsToMove){
          needsToMove = true
          context.system.scheduler.scheduleOnce(elevatorSpeedPerFloor.second, self, ReachNextFloor)
        }

        //new user request added so update the direction an destination floor
        //self ! UpdateDirectionAndDestinationFloor()


      //used when a user gets in to cancel the call request and when he gets of to cansel the target floor request
    case CanselRequest(userID) =>
      requsts -= userID
      // user request removed so if there are requests left update the direction and destination floor
      if(requsts.nonEmpty) {
        //self ! UpdateDirectionAndDestinationFloor()
      }else{
        needsToMove = false
      }


    case ReachNextFloor =>
      self ! UpdateDirectionAndDestinationFloor()

      if(needsToMove) {
        if (direction == DIRECTION.UP && currentFloor < NumofFloors) {
          currentFloor = currentFloor + 1;
        } else if(currentFloor > 0){
          currentFloor = currentFloor - 1;
        }
        println(s"Elevator:$ID reached floor: $currentFloor")


        // Notify riders of the new floor reached eather for pick up or dropoff
        val usersAtCurrentFloor = requsts.filter(_._2 == currentFloor).keys
        usersAtCurrentFloor.foreach { userID =>
          context.actorSelection(s"/user/user$userID") ! ElevatorArivedToPickUp(currentFloor)
          context.actorSelection(s"/user/user$userID") ! ArivedAtTargetFloor(currentFloor)

          //self ! CanselRequest(userID)
        }

        //if atleast one user entered of exited we are gona reach the next level at
        // elevatorPickupDropofSpeed + elevatorSpeedPerFloor else only elevatorSpeedPerFloor
        if (usersAtCurrentFloor.nonEmpty) {
          context.system.scheduler.scheduleOnce((elevatorPickupDropofSpeed+elevatorSpeedPerFloor).second, self, ReachNextFloor)
        } else {
          context.system.scheduler.scheduleOnce(elevatorSpeedPerFloor.second, self, ReachNextFloor)
        }

      }else{
        if(requsts.nonEmpty){
          //needsToMove = true
        }
      }

  }

}

object Elevator {
  def props(ID: Int, NumofFloors: Int): Props = Props(new Elevator(ID, NumofFloors))
}
