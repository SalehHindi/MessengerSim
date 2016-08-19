// Hello World in actor model

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

class HelloActor(myName: String) extends Actor {
  def receive = {
    case "hello" => println("hello Im %s".format(myName))
    case _       => println("huh?")
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")
  // default Actor constructor
  // create an actor with the "actorOf(Props[TYPE])" syntax
  // val helloActor = system.actorOf(Props[HelloActor], name = "helloactor")
  // If the Actor constructor takes a string and we want to pass in "fred" use the below syntax
  val helloActor = system.actorOf(Props(new HelloActor("Fred")), name = "helloactor")
  helloActor ! "hello"
  helloActor ! "buenos dias"
}