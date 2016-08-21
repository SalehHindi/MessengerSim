// Messaging system local test

import akka.actor._
import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._

case class Whatever(arg1: String)
case class Initialize(listOfAllActors: ListBuffer[ActorRef])
case object FirstMessage
case class Message(messageContent: String)
case class FriendRequest(targetFriend: ActorRef)
case class RemoveFriend(targetFriend: ActorRef)


class generalUser(redis: RedisClient) extends Actor {
	// val redis: RedisClient = redis  This may not be needed
	var activeConversations: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
	var contacts: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
	var conversationCounter: Int = 0
	val conversationLength: Int = 20

	def receive = new scala.PartialFunction[Any, Unit ] {
		def apply(message: Any): Unit = message match {
			case Initialize(_:ListBuffer[ActorRef]) =>
				// After initializing all the actors, we need to pass in all the Actors in the system to each actor
				val theActorList: ListBuffer[ActorRef] = message.asInstanceOf[Initialize].listOfAllActors
				println("Initialize")

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
					println(theMessage)

					val dialogue = Await.result(redis.lpop("server:grimm_fairy_tales"), Duration(100, MILLISECONDS)).get.utf8String
					// Await.result(dialogue, 5 seconds)

					sender ! Message("%s: %s".format(self.path.name, dialogue))
					conversationCounter += 1
					
					redis.rpush("server:chat_logs:%sV%s".format(self.path.name, sender.path.name), "%s: %s".format(self.path.name, dialogue))
					redis.rpush("server:chat_logs:%sV%s".format(sender.path.name, self.path.name), "%s: %s".format(self.path.name, dialogue))
				
				} else {
					println("Conversation over")
					redis.rpush("server:chat_logs:%sV%s".format(self.path.name, sender.path.name), "Conversation Over") 
					redis.rpush("server:chat_logs:%sV%s".format(sender.path.name, self.path.name), "Conversation Over") 
				}

			case FriendRequest(_:ActorRef) => 
				// When someone sends a friends request to add to a list of their contacts
				// Right now we go from friends request -> start convo with new contact -> Conversation
				println("FriendRequest")
				val theFriend: ActorRef = message.asInstanceOf[FriendRequest].targetFriend

				contacts += theFriend

				contacts(contacts.length - 1) ! FirstMessage

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
		var allActors: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
		
		def createUser(userCount: Int): ListBuffer[ActorRef] = {
			if (userCount > 0) {
				allActors += system.actorOf(Props(new generalUser(redis)), name = "%s".format(userCount))
				createUser(userCount -1)
			} else {
				allActors
			}
		}

		createUser(numberOfUsers)
	}

	implicit val system = ActorSystem("AllPeople")
	val redis: RedisClient = RedisClient()

	var allActors: ListBuffer[ActorRef] = createUsers(10, redis)	    // Create all users

	allActors(0) ! Initialize(allActors)						// Awaken
	allActors(0) ! FriendRequest(allActors(1))					// Friend Request
	// These above two lines create deadletters
	// allActors(0) ! FriendRequest(allActors(2))				// Friend Request

	// allActors(1) ! allActors									// Awaken
	// allActors(1) ! allActors(3)								// Friend Request
	// allActors(1) ! allActors(4)								// Friend Request


	// programLoop = {
	//  Send out all friend requests for this loop
	//  Start all conversations for this loop
	//  Send out all remove requests
	//  Wait a second or some time
	// }
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
    Load up Shakespeare to have realistic looking chats
  Plan out distribution of
  	friend requests
  	who talks to who
  	chat durations
  Make this span multiple machines
  ⚑⚑⚑⚑⚑⚑ V1 DONE ⚑⚑⚑⚑⚑⚑

*/

/*
Design considerations
1) Chat logs should be the same log for both parties. Printing to redis twice generally solves
   this but the asychronous nature of the Actors makes it hard to keep the messages in the right order.
   Perhaps messages need to have a timestamp and a Queue collects and sorts the messages and puts them 
   in the right order....
*/

// To load shakespeare chat, run a script that adds a shit ton of skake to redis is a list
// then lpop that list to get the chat in order.
// 
//
//