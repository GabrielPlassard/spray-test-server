package com.excilys.spraytestserver

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object Boot extends App with SslConf{

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "demo-service")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}



// for SSL support (if enabled in application.conf)
trait SslConf {

  // if there is no SSLContext in scope implicitly the HttpServer uses the default SSLContext,
  // since we want non-default settings in this example we make a custom SSLContext available here
  implicit def sslContext: SSLContext = {
    val keyStoreResource = "/ssl-test-keystore.jks"
    val password = ""

    val keyStore = KeyStore.getInstance("jks")
    keyStore.load(getClass.getResourceAsStream(keyStoreResource), password.toCharArray)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }
  /*
// if there is no ServerSSLEngineProvider in scope implicitly the HttpServer uses the default one,
// since we want to explicitly enable cipher suites and protocols we make a custom ServerSSLEngineProvider
// available here
implicit def sslEngineProvider: ServerSSLEngineProvider = {
ServerSSLEngineProvider { engine =>
engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
engine
}
}                                                          */
}