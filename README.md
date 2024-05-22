# Elevatoor Simulator using Akka actors in Scala

## use
after configuring the the val in each actor to the desired values(the elevator speed per floor,
the drop off and pick up speed,and the users max wait time) you can run it with the following command line args

- the number of elevators
- the number of floors
- the numbers of users
- and the time you want the simulation to run for

### DISCLAIMER
right know the project is configured to have as a unit of time seconds 
and not minutes as for ease of use to configure it to minutes just 
change in the  first arg of the context.system.scheduler.scheduleOnce 
the .seconds to .minutes

## implementation
- the main object creates the elevator and user Actor and then
sends a WaitAndRequestElevator message to all the user actors this case
picks in random a target floor that the user will go to, a elevator to use 
and the time that he is going to wait before calling the elevator finally 
it schedules a message RequestElevator to be sent to it self in the times
 
- the RequestElevator case just sents a ButtonPressed message(passing his id and the his currentfloor)
to the previusly selected elevator to signal that the user call the elevator to pick his up form the floor he is at

- the elevator resives this message and adds the request to the request map(maps the id of the user with a floor, ether
the floor they want to be picked up from or the floor they want to be drooped offed)
and if the elevator has the needsToMove set to false(had no requests)
we set it to true and and schedules a ReachNextFloor message to be sent to itself oncce elevatorSpeedPerFloor time has passed

- the ReachNextFloor case if the elevator needs to move firstly sets to itself a UpdateDirectionAndDestinationFloor meesage that case 
according to the direction the elevator is going maxRequestedFloor and the minRequestedFloor updates the direction
and the destination floor then the ReachNextFloor contenius by updating the current floor according to the direction
and then for each request that the value of the map is == to the current floor it sents to the user that made the request
a ElevatorArivedToPickUp and a ArivedAtTargetFloor message
the it schedules a ReachNextFloor message to be set to itself onse elevatorPickupDropofSpeed+elevatorSpeedPerFloor or just
elevatorSpeedPerFloor time has passed according to if a any user got on or off the elevator at that floor

- the ElevatorArivedToPickUp and ArivedAtTargetFloor check if the current floor of the elevaor is theire current level(for pickup)
or their target leverl(for drop off) if true they get of the elevator update they current floor and sent a message to the 
elevator they used to cancel threir requist as it is now fulfilled. then they sent to them selfs a WaitAndRequestElevator
message to start all over again

- finally the CanselRequest case of the elevator just removes the map form the requests and if the request map is now empty
it dicables the elevator (using the needsToMove flag) untill the next request cames.