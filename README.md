# MessengerSim - V1

## Challenge
I have long been fascinated by the ability of WhatsApp and Facebook Messenger to scale to billions of users. I wanted to see first hand how hard it would be to build scalable asynchronous system so I built MessengerSim. MessengerSim uses the Akka library and it's Actor model for asynch communication to build a simulation of a messenging platform. The challenge is to build an accurate simulation that scales so that 10MM simulated can have simulataneous conversations with proof that it can scale to 100M and 1BB users. 

## Running
1. To run the simulation, run `git clone https://github.com/SalehHindi/MessengerSim.git`.
2. `cd` to the directory. 
3. Run `redis-server` to start redis. To check if redis is running the command `redis-cli ping` should return `PONG`. 
4. Run `python loadGrimm.py` to load redis with the mock conversation data we will use. 
5. Run `sbt run` to compile and run the simulation. Note that at the end of the simulation, the program will not terminate by itself and you'll need to send a keyboard interrupt to exit.

### Dependencies
1. Python
2. redis python package (can be installed with `pip install redis`) 
3. redis-server
4. Scala
5. sbt (simple build tool)
6. Java-1.8
7. Preferrably, access to multiple machines or Amazon Web Services so you can distribute the system.

## Implementation
### Akka Library
The Actor model is [look up wikipedia]

These main cases represent the possible actions an actor can take:
Initialize -- Adds the actors to the database of available actorss.
FriendRequest -- Adds both actorss to each other's contacts so they may message each other.
FirstMessage -- Displays a greeting to both actorss and start message chain.
Messenge -- Sends message. [Describe message queue to make sure they are in the same order]

### Distribution of Conversations
In real life, people don't talk to everyone. They talk to a distribution of friends with a person chatting a lot with a few of their closest friends and chatting for not very long and infrequently with a lot of people. To simulate this, conversations are started according to this function: [conversation distribution] [picture of distribution]

### Bottlenecks
I initially thought the biggest constraint on the number of users is memory but it seems to be processing on the cpu. [do some fermi calculations for memory vs cpu]

### Security
Speculate on security. If I were to add security to the conversations, this is what it would look like.
It would be demanding on cpu.


## Future Plans
V2 - Dockerize the app
   - Message queue