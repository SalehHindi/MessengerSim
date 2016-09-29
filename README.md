# MessengerSim - V1

## Challenge
I have long been fascinated by the ability of WhatsApp and Facebook Messenger to scale to billions of users. I wanted to see first hand how hard it would be to build scalable asynchronous system so I built MessengerSim. MessengerSim uses the Akka library and it's Actor model for asynchronous communication to build a simulation of a messenging platform. The challenge is to build a simulation of distributed conversations that scales to 1MM concurrent conversations. 

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

The program works by creating n actors. Then each actor sends a friend request to every other actor so that each actor can have all other actors in its list of contacts. Then all the actors send a message to all the actors in its contacts and a conversation is started between each actor and all other actors. This forms a complete graph of actors. In normal conversations, most people talk to a small subset of their contacts and these conversations have lengths that vary by some distribution instead of talking to everyone for a fixed length.

### Analysis of Experiment

Currently, the program is made to run on one machine for simplicity but it can easily be configured to run on multiple machines. This is admittedly a pain in the butt to do as the IPs of each machine need to be hardcoded in the application.conf file. With one machine, a single machine runs the redis server, designated the "local" machine and the others distribute the computational workload. 

I thought that memory would be the biggest bottleneck to the performance of this program but it turned out that the program is more computationally intensive than memory intensive. To examine performance, I ran the simulation on one machine and used `htop` to look at peak CPU usage, Memory usage, and total runtime. I ran the simulation for 10, 15, and 20 agents on my personal laptop and I ran the simulation on an AWS C3.4xlarge instance for 50, 100, 150, 1000 agents. Each simulation with n agents will have n^2 asynchronous conversations.

The data is presented below. These screenshots were taken at approximately peak CPU usage.


Local Machine: 10 agents 100 conversations
![Local Machine: 10 agents 100 conversations](http://i.imgur.com/dbwjlZs.png "Local Machine: 10 agents 100 conversations")

Local Machine: 15 agents 225 conversations
![Local Machine: 15 agents 225 conversations](http://i.imgur.com/9HqqkmN.png "Local Machine: 15 agents 225 conversations")

Local Machine: 20 agents 400 conversations
![Local Machine: 20 agents 400 conversations](http://i.imgur.com/rq8sWG1.png "Local Machine: 20 agents 400 conversations")

EC2 Instance: 100 agents 10,000 conversations
![EC2 Instance: 100 agents 10,000 conversations](http://i.imgur.com/Fs0bxBX.png "EC2 Instance: 100 agents 10,000 conversations")

EC2 Instance: 150 agents 22,500 conversations
![EC2 Instance: 150 agents 22,500 conversations](http://i.imgur.com/W3RI53m.png "EC2 Instance: 150 agents 22,500 conversations")

EC2 Instance: 200 agents 40,000 conversations
![EC2 Instance: 200 agents 40,000 conversations](http://i.imgur.com/gVq76s2.png "EC2 Instance: 200 agents 40,000 conversations")

EC2 Instance: 1000 agents 1,000,000 conversations
![EC2 Instance: 1000 agents 1,000,000 conversations](http://i.imgur.com/4MA562S.png "EC2 Instance: 1000 agents 1,000,000 conversations")

As the data shows, increasing agents generally increased max CPU usage except at the EC2 1000 agent trial. Memory never hits a maximum leading me to think that the bottleneck is CPU usage. [Point about connections being dropped]. [Point about runtime] 

4. At what point should the wait for the server response timeout?

5. Message queue

6. Max CPU usage vs CPU time

### Clustering
One problem is how to distribute the actors in MessengerSim. When I ran the simulation all the machines had the same processing power 

[How does clustering work in Akka?]

## Security
Speculate on security. If I were to add security to the conversations, this is what it would look like.
It would be demanding on cpu.

## Future Plans
V2 - Dockerize the app
   - Message queue
   - Scale to XXXX number of users