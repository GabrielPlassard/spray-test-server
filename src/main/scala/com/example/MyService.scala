package com.example

import spray.routing.authentication.BasicAuth
import scala.concurrent.ExecutionContext.Implicits.global
import spray.httpx.encoding.{Deflate, Gzip}
import akka.actor.Actor
import spray.routing.HttpService
import spray.http._
import org.slf4j.LoggerFactory
import org.apache.commons.codec.digest.DigestUtils

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val logger = LoggerFactory.getLogger("com.example.MyService")

  val myRoute =
      path("") {
        ctx => ctx.complete(s"Headers :\n\t${ctx.request.headers.mkString("\n\t")} \n\nRequete.entity : \n${ctx.request.entity} \n\n")
      } ~
      path("verb"){
         ctx => ctx.complete(ctx.request.method.toString())
      } ~
      path("getQuery"){
        parameterSeq{ p =>
            ctx => ctx.complete( hashQueryParameters(p) )
         }
      } ~
      path("admin") {
        authenticate(BasicAuth()) { user =>
            ctx => ctx.complete("This is the admin page")
        }
      } ~
      path("gzipResponse") {
        encodeResponse(Gzip) {
          ctx => ctx.complete("Here is my response in gzip")
        }
      } ~
      path("deflateResponse") {
        encodeResponse(Deflate) {
           ctx => ctx.complete("Here is my response in deflate")
        }
      } ~
      path("gzipRequest") {
        decodeRequest(Gzip) {
          ctx => ctx.complete(s"Headers :\n\t${ctx.request.headers.mkString("\n\t")} \n\nRequete.entity : \n${ctx.request.entity} \n\n")
        }
      } ~
      path("deflateRequest") {
        decodeRequest(Deflate) {
          ctx => ctx.complete(s"Headers :\n\t${ctx.request.headers.mkString("\n\t")} \n\nRequete.entity : \n${ctx.request.entity} \n\n")
        }
      } ~
      path("chunkResponse") {
          // we detach in order to move the blocking code inside the simpleStringStream off the service actor
          detachTo(singleRequestServiceActor) {
            complete(simpleStringStream)
        }
      }



  // we prepend 2048 "empty" bytes to push the browser to immediately start displaying the incoming chunks
  lazy val streamStart = " " * 2048 + "<html><body><h2>A streaming response</h2><p>(for 15 seconds)<ul>"
  lazy val streamEnd = "</ul><p>Finished.</p></body></html>"

  def simpleStringStream: Stream[String] = {
    val secondStream = Stream.continually {
      // CAUTION: we block here to delay the stream generation for you to be able to follow it in your browser,
      // this is only done for the purpose of this demo, blocking in actor code should otherwise be avoided
      Thread.sleep(500)
      "<li>" + DateTime.now.toIsoDateTimeString + "</li>"
    }
    streamStart #:: secondStream.take(15) #::: streamEnd #:: Stream.empty
  }

  def hashQueryParameters(p : Seq[(String,String)]) = {
    logger.debug("Request parameters are : " + p.mkString(","))
    val concat = p.foldLeft("")( (acc,kv) => acc + kv._2).mkString("")
    logger.debug("Concatenation of values is : " + concat)
    val sha1 = DigestUtils.sha1Hex(concat)
    logger.debug("SHA-1 is : "+sha1)
    sha1
  }

}