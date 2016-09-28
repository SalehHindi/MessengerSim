# MessengerSim - V1

## Challenge
I have long been fascinated by the ability of WhatsApp and Facebook Messenger to scale to billions of users. I wanted to see first hand how hard it would be to build scalable asynchronous system so I built MessengerSim. MessengerSim uses the Akka library and it's Actor model for asynchronous communication to build a simulation of a messenging platform. The challenge is to build a simulation of distributed conversations that scale.

## Running

### Dependencies
1. Python 2.7+
2. redis python package (can be installed with `pip install redis`) 
3. redis-server
4. Scala
5. sbt (simple build tool for scala)
6. Java-1.8
7. Preferrably, access to multiple machines or Amazon Web Services so you can distribute the system.

### Running
1. To run the simulation, run `git clone https://github.com/SalehHindi/MessengerSim.git`.
2. `cd` to the directory. 
3. Run `redis-server` to start redis. To check if redis is running, the terminal command `redis-cli ping` should return `PONG`. 
4. Run `python loadGrimm.py` to load redis with the mock conversation data we will use. 
5. Run `sbt run` to compile and run the simulation. Note that at the end of the simulation, the program will not terminate by itself and you'll need to send a keyboard interrupt to exit.

## Implementation

### Akka + Actors
The Actor model is a model for reasoning about distributed systems that treat "actors" as the base agent. Actors act on a series of "messages" that are sent to them from other actors. They can also create other actors locally and remotely on a different machine. The actor model is a simply model and very well suited to a message platform, chat apps, or other systems where humans are the focus which is why I decided to use it for this project.

These main cases represent the possible actions an actor can take in MessengerSim:
- Initialize -- Adds the actors to the database of available actorss.
- FriendRequest -- Adds both actorss to each other's contacts so they may message each other.
- FirstMessage -- Displays a greeting to both actorss and start message chain.
- Messenge -- Sends message. 

[Generally how the program works]

### Analysis of Experiment
Some considerations are 
1. How the actors are stored (Vector vs List)
2. How is the servers are distributed
3. Bottlenecks (CPU vs Memory)
4. At what point should the wait for the server response timeout?
5. Message queue

### Clustering
One problem is how to distribute the actors in MessengerSim. When I ran the simulation all the machines had the same processing power [List processing power]

[How does clustering work in Akka?]

## Security
Speculate on security. If I were to add security to the conversations, this is what it would look like.
It would be demanding on cpu.

## Future Plans
V2 - Dockerize the app
   - Message queue
   - Scale to XXXX number of users