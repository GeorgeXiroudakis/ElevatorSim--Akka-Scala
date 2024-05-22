package elevatorSim


import akka.actor._


object Main {
    def main(args: Array[String]): Unit = {
        if(args.length != 4){
            println("invalid call of prog.\nPlease pass 4 argument the number of elevators, the number of floors, " +
              "the number of users, and the time you want the simulation to run for ")

            sys.exit(1)
        }

        val NumOfElevators = args(0).toInt
        val NumOfFloors = args(1).toInt
        val NumOfUsers = args(2).toInt
        val MinutesToSimulate = args(3).toInt

        //create actor system
        val system = ActorSystem("ElevatorSimulationSystem")

      // Create Elevator actors with unique IDs
        val elevators = (1 to NumOfElevators).map { id =>
          system.actorOf(Elevator.props(id, NumOfFloors), s"elevator$id")
        }
      // Create User actors with unique IDs
        val users = (1 to NumOfUsers).map { id =>
          system.actorOf(User.props(id, NumOfFloors, elevators), s"user$id")
        }

        //start all the users
        users.foreach { user =>
          user ! WaitAndRequestElevator
        }

      // Schedule to shut down the system after the simulation time
        import system.dispatcher
        import scala.concurrent.duration._
        system.scheduler.scheduleOnce(MinutesToSimulate.seconds) {
          system.terminate()
        }

    }


}
