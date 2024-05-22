package elevatorSim

import akka.actor.*

import scala.util.Random
import scala.concurrent.duration._

case class WaitAndRequestElevator()

case class RequestElevator()

case class ElevatorArivedToPickUp(cfoor: Int)

case class ArivedAtTargetFloor(cfoor: Int)


class User (val ID: Int, val numOfFloors: Int, elevatorRefs: Seq[ActorRef]) extends Actor {
  import context.dispatcher

  val MaxWaitTime = 5

  var startTimer: Long = 0
  var endTimer: Long = 0

  var currentFloor: Int = 0
  var targetFloor: Int = 0   //everyone starts on the floor zero
  var elevatorInUse: Int = 0

  def receive: Receive = {

    case WaitAndRequestElevator =>
      //select a random floor

      targetFloor = Random.nextInt(numOfFloors)
      while (targetFloor == currentFloor)targetFloor = Random.nextInt {
        numOfFloors
      }

      elevatorInUse = Random.nextInt(elevatorRefs.length)

      //wait a random time and then request the Elevator
      val waitTime = Random.nextInt(MaxWaitTime) + 1
      println(s"User:$ID is waiting at floor:$currentFloor for $waitTime minutes")
      context.system.scheduler.scheduleOnce(waitTime.seconds, self, RequestElevator())

    case RequestElevator() =>
      //call the elevator to come to you
      elevatorRefs(elevatorInUse) ! ButtonPressed(ID, currentFloor)
      val displayElevatorNum = elevatorInUse+1
      println(s"User:$ID calls elevator:$displayElevatorNum to go from floor:$currentFloor to floor:$targetFloor")
      startTimer = System.nanoTime()

      //check if the elevator came to pick me up
    case ElevatorArivedToPickUp(cfoor: Int) =>
      if(currentFloor == cfoor){
        val displayElevatorNum = elevatorInUse+1
        endTimer = System.nanoTime()
        val elapsedTime = (endTimer - startTimer) / 1e9
        println(s"User:$ID got on the elevator$displayElevatorNum to go to floor:$targetFloor after waiting $elapsedTime secs")

        elevatorRefs(elevatorInUse) ! CanselRequest(ID)

        //press the button to go to target floor
        elevatorRefs(elevatorInUse) ! ButtonPressed(ID, targetFloor)
      }

    case ArivedAtTargetFloor(cfoor: Int) =>
      if(targetFloor == cfoor) {
        val displayElevatorNum = elevatorInUse+1
        println(s"User:$ID got to taget floor:$targetFloor using the elevator:$displayElevatorNum")
        currentFloor = targetFloor

        elevatorRefs(elevatorInUse) ! CanselRequest(ID)

        //wait a random time and call an elevator again
        self ! WaitAndRequestElevator
      }
  }
}

object User {
  def props(ID: Int, numOfFloors: Int, elevatorRefs: Seq[ActorRef]): Props = Props(new User(ID, numOfFloors, elevatorRefs))
}
