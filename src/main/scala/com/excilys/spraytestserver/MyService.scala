package com.excilys.spraytestserver

import spray.routing.authentication.BasicAuth
import scala.concurrent.ExecutionContext.Implicits.global
import spray.httpx.encoding.{Deflate, Gzip}
import akka.actor.Actor
import spray.routing.HttpService
import spray.http._
import org.apache.commons.codec.digest.DigestUtils
import MediaTypes._
import HttpHeaders._
import com.typesafe.scalalogging.slf4j.Logging
import CacheDirectives._
import com.excilys.spraytestserver.Utils.Expires

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
trait MyService extends HttpService with Logging {

  var cacheCalls = 0

  val myRoute =
      path("") {
        ctx => ctx.complete(s"Headers :\n\t${ctx.request.headers.mkString("\n\t")} \n\nRequete.entity : \n${ctx.request.entity} \n\n")
      } ~
      path("statusCode"){
         parameter("value"){ p =>
            ctx => ctx.complete(StatusCode.int2StatusCode(p.toInt))
         }
      } ~
      path("verb"){
         ctx => ctx.complete(ctx.request.method.toString())
      } ~
      path("hashQueryParameters"){
        parameterSeq{ p =>
            ctx => ctx.complete( hashQueryParameters(p) )
         }
      } ~
      path("htmlIpsum"){
        respondWithMediaType(`text/html`) {
          ctx => ctx.complete( Utils.htmlIpsum )
        }
      } ~
      path("soapXml"){
          respondWithMediaType(`text/xml`) {
            ctx => ctx.complete( Utils.soapXml )
          }
      } ~
      path("jsonObject"){
        respondWithMediaType(`application/json`){
          ctx => ctx.complete( Utils.jsonObject )
        }
      } ~
      path("jsonArray"){
        respondWithMediaType(`application/json`){
          ctx => ctx.complete( Utils.jsonArray )
        }
      } ~
      pathPrefix("cookies"){
         path("take"){
           parameterMap{p =>
             val name = p.get("name").getOrElse[String]("name")
             val value = p.get("value").getOrElse[String]("value")
             val path = p.get("path").getOrElse[String]("/cookies")
             respondWithHeader(`Set-Cookie`(HttpCookie(name,value, path = Some(path) ))) {
               ctx => ctx.complete("Here is a cookie for you")
             }
           }
         } ~
         path("countCookies"){
             ctx => ctx.complete(s"You have ${ctx.request.cookies.size} cookie(s) for me")
         }
      } ~
      path("countCookies"){
        ctx => ctx.complete(s"You have ${ctx.request.cookies.size} cookie(s) for me")
      } ~
      pathPrefix("cache") {
        path("expires") {
          respondWithHeader(Expires(DateTime(2050, 12, 25))) {
            ctx => {
              cacheCalls += 1
              ctx.complete(s"You called me ${cacheCalls} time(s)")
            }
          }
        } ~
        path("maxAge"){
          respondWithHeader(`Cache-Control`( `max-age`(300) )) {
            ctx => {
              cacheCalls += 1
              ctx.complete(s"You called me ${cacheCalls} time(s)")
            }
          }
        } ~
        path("reset"){
          ctx => {
            cacheCalls = 0
            ctx.complete("Ok")
          }
        }
      } ~
      path("redirect"){
        parameterMap{ p =>
          redirect(p.get("target").getOrElse[String]("/"), StatusCodes.SeeOther)
        }
      } ~
      path("admin") {
        authenticate(BasicAuth()) { user =>
            ctx => ctx.complete("This is the admin page")
        }
      } ~
      path("gzipResponse") {
        encodeResponse(Gzip) {
          parameterMap{ p =>
            ctx => ctx.complete(p.get("response").getOrElse("Gzip response"))
          }
        }
      } ~
      path("deflateResponse") {
        encodeResponse(Deflate) {
           parameterMap{ p =>
             ctx => ctx.complete(p.get("response").getOrElse("Deflate response"))
           }
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
      } /*~
      path("chunkResponse") {
          // we detach in order to move the blocking code inside the simpleStringStream off the service actor
          detachTo(singleRequestServiceActor) {
            complete(simpleStringStream)
        }
      }*/



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