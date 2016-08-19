// Messaging system local test

import akka.actor._
import scala.collection.mutable.ListBuffer

case class ActorAwaken(arg1: String)
case class FirstMessage(arg1: String)
case class Message(arg1: String)
case class FriendRequest(arg1: String)
case class RemoveFriend(arg1: String)

class generalUser extends Actor {
	def receive = {
		case ActorAwaken(_) => println("Awaken")
		case FirstMessage(_) => println("First")
		case Message(_) => println("Message")
		case FriendRequest(_) => println("FriendRequest")
		case RemoveFriend(_) => println("RemoveFriend")
		case _ => println("ERROR")
	}
}

class User1(otherUser: ActorRef, allActors: List[String]) extends Actor {
	var messageCount = 0
	val maxMessages = 4
	val name = "Fred"
	val conversation: List[String] = List("No I dont have money!", 
										"Its ok", 
										"Yes it is", 
										"This is boring",
										"Ok fine Ill stay")

	def receive = {
		case "startConvo" =>
			println("New conversation between Amy and Fred")
			otherUser ! Message("Hi")

		case Message(_) =>
			// var arg2 = arg1
			if (messageCount < maxMessages) {
				println("%s: %s".format(name, conversation(messageCount)))
				println(allActors(0))
				messageCount += 1
				otherUser ! Message("Hi")

			} else {
				println("conversation over")
				context.stop(self)
			}

		case _ =>
			println("what??")	
	}
}

class User2 extends Actor {
	var messageCount = 0
	val name = "Amy"
	val conversation: List[String] = List("Do you have any money?", 
									"Sorrry..", 
									"Its nice weather huh?", 
									"Do you like ducks?",
									"Please say!")
	def receive = {
		case Message(_) =>
			println("%s: %s".format(name, conversation(messageCount)))
			messageCount += 1

			sender ! Message("Hi")
		case _ =>
			println("ERROR")	
	}
}

object localMessaging extends App {
	val theList: List[String] = List("Amy", "Bob", "Candy")

	val system = ActorSystem("TwoPeople")
	val amyActor = system.actorOf(Props[User2], name = "user2")
	val fredActor = system.actorOf(Props(new User1(amyActor, theList)), name = "user1")
	fredActor ! "startConvo"

	val system1 = ActorSystem("AllPeople")
	// val allActors: List[ActorRef] = List.fill(10)(system1.actorOf(Props[generalUser], name = "user"))

	def createUsers(numberOfUsers: Int): ListBuffer[ActorRef] = {
		var allActors: ListBuffer[ActorRef] = ListBuffer[ActorRef]()
		var userCount = numberOfUsers
		
		def createUser(userCount: Int): ListBuffer[ActorRef] = {
			if (userCount > 0) {
				allActors += system1.actorOf(Props[generalUser], name = "%s".format(userCount))
				createUser(userCount -1)
			} else {
				return allActors
			}

		}

		return createUser(numberOfUsers)
	}

	var allActors: ListBuffer[ActorRef] = createUsers(10)
	allActors(0) ! ActorAwaken("bbb")

	def programLoop = {
		// Start up a bunch of Actors 
		// Send the Actor Awaken command to each of them
		// Choose a random actor to start the conversation with.
	}
}