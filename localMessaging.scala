// Messaging system local test
// Not important: 9367e69399356d45d6b8cc408314a47c2c98c02d

import akka.actor._
import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec

case object ActorAwaken
case object FirstMessage
case object Message
case object FriendRequest
case object RemoveFriend

case class Whatever(arg1: String)

class generalUser extends Actor {
	var activeConversations: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
	var contacts: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
	val conversation: List[String] = List("a",
											"b",
											"c",
											"d",
											"e",
											"f",
											"g",
											"h",
											"i",
											"j",
											"k",
											"l")
	var conversationCounter: Int = 0 //
	val conversationLength: Int = conversation.length

	def receive = new scala.PartialFunction[Any, Unit ] {
		def apply(message: Any): Unit = message match {
			case _:ListBuffer[ActorRef] =>
				// After initializing all the actors, we need to pass in all the Actors in the system to each actor
				println("it worked!")	  
				println(self)	  

			case FirstMessage => 
				// When someone sends the first message.
				println("First")
				sender ! "%s: ))))))".format(self.path.name)

			case _:String => 
				// When someone sends any message
				if (conversationCounter < conversationLength) {
					println(message)
					sender ! "%s: %s".format(self.path.name, conversation(conversationCounter))
					conversationCounter += 1
				} else {
					println("Conversation over")					
				}

			case _:ActorRef => 
				// When someone sends a friends request to add to a list of their contacts
				// Right now we go from friends request -> start convo with new contact -> Conversation
				var theMessage = message.asInstanceOf[ActorRef]
				println("FriendRequest")
				contacts += theMessage

				contacts(contacts.length - 1) ! FirstMessage
				println(contacts.length)

			case RemoveFriend => 
				// When someone removes someone else from a list of their contacts
				println("RemoveFriend")

			case Whatever(_) =>
				var theMessage = message.asInstanceOf[Whatever]
				println(theMessage.arg1)

			case _ => 
				println("it didn worked!")	  
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
	def createUsers(numberOfUsers: Int): ListBuffer[ActorRef] = {
		@tailrec
		var allActors: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
		
		def createUser(userCount: Int): ListBuffer[ActorRef] = {
			if (userCount > 0) {
				allActors += system.actorOf(Props[generalUser], name = "%s".format(userCount))
				createUser(userCount -1)
			} else {
				allActors
			}
		}

		createUser(numberOfUsers)
	}

	val system = ActorSystem("AllPeople")
	var allActors: ListBuffer[ActorRef] = createUsers(10)	// Create all users

	allActors(0) ! Whatever("boya!")

	allActors(0) ! allActors								// Awaken
	allActors(0) ! allActors(1)								// Friend Request
	// allActors(0) ! allActors(2)								// Friend Request

	// allActors(1) ! allActors								// Awaken
	// allActors(1) ! allActors(2)								// Friend Request
	// allActors(1) ! allActors(3)								// Friend Request


	// println(allActors(0))
	// println(allActors)

	// Awaken all actors

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
  Build base behavior of each actor
  Make this span multiple machines

*/