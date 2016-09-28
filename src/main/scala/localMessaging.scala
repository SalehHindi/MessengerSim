// Messaging system local test

import akka.actor._
import akka.remote._
import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import java.net._

case class Initialize(localActors: ListBuffer[ActorRef])
case object FirstMessage
case class Message(messageContent: String)
case class FriendRequest(targetFriend: ActorSelection)
case class RemoveFriend(targetFriend: ActorRef)


class generalUser(redis: RedisClient) extends Actor {
	var activeConversations: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
	var contacts: ListBuffer[ActorSelection] = ListBuffer[ActorSelection]()
	var conversationCounter: Int = 0
	val conversationLength: Int = 30
	// val remote = context.actorSelection("akka.tcp://AllPeople@%s:5150/user/1".format("52.89.11.41"))

	def receive = new scala.PartialFunction[Any, Unit ] {
		def apply(message: Any): Unit = message match {
			case Initialize(_:ListBuffer[ActorRef]) =>
				// After initializing all the actors, we need to pass in all the Actors in the system to each actor
				val theActorList: ListBuffer[ActorRef] = message.asInstanceOf[Initialize].localActors
				val pathAsString : String = self.path.toStringWithAddress(self.path.address)
				redis.rpush("server:ActorMasterList", pathAsString)


			case FirstMessage => 
				// When someone sends the first message.

				// Clear the chat at the beginning of each 
				redis.del("server:chat_logs:%sV%s".format(self.path.name, sender.path.name))
				redis.del("server:chat_logs:%sV%s".format(sender.path.name, self.path.name))

				redis.rpush("server:chat_logs:%sV%s".format(sender.path.name, self.path.name), "Chat between %s and %s has started".format(self.path.name, sender.path.name))
				redis.rpush("server:chat_logs:%sV%s".format(self.path.name, sender.path.name), "Chat between %s and %s has started".format(self.path.name, sender.path.name))

				sender ! Message("%s: Greetings!".format(self.path.name))
				redis.rpush("server:chat_logs:%sV%s".format(self.path.name, sender.path.name), "%s: Greetings!".format(self.path.name))
				redis.rpush("server:chat_logs:%sV%s".format(sender.path.name, self.path.name), "%s: Greetings!".format(self.path.name))
				

			case Message(_:String) => 
				// When someone sends any message
				val theMessage: String = message.asInstanceOf[Message].messageContent

				if (conversationCounter < conversationLength) {
					val dialogue = Await.result(redis.lindex("server:grimm_fairy_tales", conversationCounter), Duration(4000, MILLISECONDS)).get.utf8String

					sender ! Message("%s: %s".format(self.path.name, dialogue))
					conversationCounter += 1
					
					redis.rpush("server:chat_logs:%sV%s".format(self.path.name, sender.path.name), "%s: %s".format(self.path.name, dialogue))
					redis.rpush("server:chat_logs:%sV%s".format(sender.path.name, self.path.name), "%s: %s".format(self.path.name, dialogue))
				
				} else {
					println("Conversation over")
					redis.rpush("server:chat_logs:%sV%s".format(self.path.name, sender.path.name), "Conversation Over") 
					redis.rpush("server:chat_logs:%sV%s".format(sender.path.name, self.path.name), "Conversation Over") 
				}

			case FriendRequest(_:ActorSelection) => 
				// When someone sends a friends request to add to a list of their contacts
				// Right now we go from friends request -> start convo with new contact -> Conversation
				val theFriend: ActorSelection = message.asInstanceOf[FriendRequest].targetFriend
				
				contacts += theFriend

				contacts(contacts.length - 1) ! FirstMessage

				// remote ! FirstMessage


			case RemoveFriend(_:ActorRef) => 
				// When someone removes someone else from a list of their contacts
				// Not currently used...
				val theFriend: ActorRef = message.asInstanceOf[RemoveFriend].targetFriend
				println("RemoveFriend")

			case _ => 
				println("ERROR!")	  
				println(message)	  
		}

		def isDefinedAt(x: Any): Boolean = x match {
			// I may not need this function....
			case "hiii" => true
			case _ => true
		}
	} 
}

object localMessaging extends App {
	def createUsers(numberOfUsers: Int, redis: RedisClient): ListBuffer[ActorRef] = {
		@tailrec
		var localActors: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
		
		def createUser(userCount: Int): ListBuffer[ActorRef] = {
			if (userCount > 0) {
				localActors += system.actorOf(Props(new generalUser(redis)), name = "%s".format(userCount))
				createUser(userCount -1)
			} else {
				localActors
			}
		}

		createUser(numberOfUsers)
	}

	def initializeAllUsers(numberOfUsers: Int): Unit = {
		for (i <- 0 to numberOfUsers-1){
			localActors(i) ! Initialize(localActors)
		}
	}

	def addUsersToRemoteUsersList(numberOfUsers: Int): Unit = {
		redis.del("server:ActorMasterList")
		// redis.del("server:ActorMasterList")

		// Push all local the actors to redis
		for (i <- 0 to numberOfUsers-1){
			var pathAsString : String = localActors(i).path.toStringWithAddress(localActors(i).path.address)
			redis.rpush("server:ActorMasterList", pathAsString)
		}

		// Add list of remote IPs to the database. 
		// I will need a more programmatic way to do this...
		val IPs: ListBuffer[String] = ListBuffer[String]()
		IPs += "52.89.11.41"
		// IPs += "52.89.11.41"
		// IPs += "52.89.11.41"
		// IPs += "52.89.11.41"
		// IPs += "52.89.11.41"

		for (IP <- IPs.toIterable) {
			redis.rpush("server:IPList", IP)
		}
	}

	def addRemoteUsers(numberOfUsers: Int): Unit = {
		// Pop a single IP from the database 
		val x = Await.result(redis.lpop("server:IPList"), Duration(5000, MILLISECONDS))
		// println(x(0).utf8String)

		// Make a context from it


		// Get all of the actors from that system

		// We add as many remote users as we have created users. 
		// This presents a problem with distributing loads.
		for (i <- 0 to numberOfUsers-1) {
			// we need a context
			// localActors += context.actorFor("akka.tcp://AllPeople@%s:5150/user/%s".format(RemoteIP, i))
		}

		// localActors
	}

	def sendFriendRequestsToAll(numberOfUsers: Int, system: ActorSystem): Unit = {
		val ActorVector = Await.result(redis.lrange("server:ActorMasterList", 0, -1), Duration(5000, MILLISECONDS))

		// for (i2 <- xxx.toIterable) { println("aaaa"); println(i2.utf8String)}


		for (ActorByteString1 <- ActorVector.toIterable){
			for (ActorByteString2 <- ActorVector.toIterable){
				system.actorSelection(ActorByteString1.utf8String) ! FriendRequest(system.actorSelection(ActorByteString2.utf8String)) 
				// Send messages to local actors too....
			}
		}
	}

	implicit val system = ActorSystem("AllPeople")
	val remoteRedisIP = "127.0.0.1"
	val redis: RedisClient = new RedisClient(remoteRedisIP, 6379)
	redis.del("server:ActorMasterList")

	val ActorNumber: Int = 3
	var localActors: ListBuffer[ActorRef] = createUsers(ActorNumber, redis)	    // Create all users
	var remoteActors: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
	initializeAllUsers(ActorNumber)

	// Testing connecting to remote Actor

	// addUsersToRemoteUsersList(ActorNumber)
	// addRemoteUsers(ActorNumber)
	sendFriendRequestsToAll(ActorNumber, system)
	// localActors(0) ! FriendRequest(system.actorSelection(localActors(1).path))


	// localActors(0) ! FriendRequest(system.actorSelection("/user/6"))	

	// // val xxx = system.actorSelection("akka://AllPeople/user/4")
	// localActors(1) ! FriendRequest(xxx)

	// We might need to do this for all remote actors. remote is the name of the actor so you can remote ! Etc
	// val remote = context.actorFor("akka://HelloRemoteSystem@127.0.0.1:5150/user/RemoteActor")}
}
/*

TODO:
✓ Research actor model
✓ Create System to run local async message system
✓ Add to github
✓ Allow messages to contain arguments
✓ Build base behavior of each actor
✓ Add Redis functionality
✓   Save chats
✓   Load up Shakespeare to have realistic looking chats
✓ Make this span multiple machines
✓  	connect to remote redis server
  	connect to remote actor 
  Plan out distribution of
  	friend requests
  	who talks to who
  	chat durations
  ⚑⚑⚑⚑⚑⚑ V1 DONE ⚑⚑⚑⚑⚑⚑

*/

/*
Design considerations
1) Chat logs should be the same log for both parties. Printing to redis twice generally solves
   this but the asychronous nature of the Actors makes it hard to keep the messages in the right order.
   Perhaps messages need to have a timestamp and a Queue collects and sorts the messages and puts them 
   in the right order....
2) Should we have 1 redis server with all the info (text scripts, available actors, chat logs?)
   OR a server for every machine running?
*/

/*
Each Actor (name=RemoteActor) for System (HelloRemoteSystem) has 
the address akka://HelloRemoteSystem@127.0.0.1:5150/user/RemoteActor

On generation of an actor, the actor's address is added to server:ActorMasterList
Then all the actors are loaded into the local system by reading server:ActorMasterList
	-We can save an actor's path via self.path or sender.path
	-And we can get the ActorRef with that path via system.actorSelection("/user/10")


Then all messaging/friend requests in the main loop can start


*/
