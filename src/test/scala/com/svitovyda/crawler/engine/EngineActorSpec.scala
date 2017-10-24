package com.svitovyda.crawler.engine

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.svitovyda.crawler.engine.UrlService.Url
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.Matchers._

import scala.concurrent.duration._


class EngineActorSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with Eventually
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "CounterActor" must {

    "add correct URL" in {
      val actor = system.actorOf(EngineActor.props(system.dispatcher))

      actor ! EngineActor.Request.AddUrl("http://gmail.com")
      expectMsgType[EngineActor.Response.Count]
      actor ! EngineActor.Request.AddUrl("http://www.scala-lang.org")
      expectMsgPF(1.second) { case EngineActor.Response.Count(i) if i >= 2 => i }
    }

    "add and then remove fake URL" in {
      val actor = system.actorOf(EngineActor.props(system.dispatcher))

      actor ! EngineActor.Request.AddUrl("http://some.non.existing")
      expectMsgType[String]

      actor ! EngineActor.Request.GetCount
      expectMsg(EngineActor.Response.Count(1))

      eventually {
        actor ! EngineActor.Request.GetCount
        expectMsg(EngineActor.Response.Count(0))
      }
    }

    "return list of links" in {
      val actor = system.actorOf(EngineActor.props(system.dispatcher))

      actor ! EngineActor.Request.AddUrl("http://gmail.com")
      actor ! EngineActor.Request.AddUrl("http://www.scala-lang.org")

      actor ! EngineActor.Request.GetUrls
      val EngineActor.Response.URLs(list) = expectMsgType[EngineActor.Response.URLs]
      list should contain allOf (Url("http://gmail.com"), Url("http://www.scala-lang.org"))
    }

    "not return content for broken page" in {
      val actor = system.actorOf(EngineActor.props(system.dispatcher))

      actor ! EngineActor.Request.AddUrl("http://some.non.existing")
      expectMsgType[String]

      actor ! EngineActor.Request.GetPageContent("http://some.non.existing")
      expectMsgType[String]
    }

    "return content of valid page" in {
      val actor = system.actorOf(EngineActor.props(system.dispatcher))

      actor ! EngineActor.Request.AddUrl("http://google.com")
      expectMsgType[EngineActor.Response.Count]

      eventually(timeout(3.seconds), interval(300 milliseconds)) {
        actor ! EngineActor.Request.GetPageContent("http://google.com")
        expectMsgType[EngineActor.Response.Content]
      }
    }

  }
}
