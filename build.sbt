import AssemblyKeys._ // put this at the top of the file

assemblySettings

organization  := "com.excilys"

name          := "spray-test-server"

version       := "1.1"

scalaVersion  := "2.10.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://nightlies.spray.io"
)

libraryDependencies ++= Seq(
  "io.spray"            	    %   "spray-can"         	    % "1.2-20130912",
  "io.spray"            	    %   "spray-routing"     	    % "1.2-20130912",
  "io.spray"            	    %   "spray-testkit"     	    % "1.2-20130912"    % "test",
  "com.typesafe.akka"   	    %%  "akka-actor"        	    % "2.2.1",
  "com.typesafe.akka"   	    %%  "akka-testkit"      	    % "2.2.1"           % "test",
  "org.scalatest"               %%  "scalatest"                 % "2.0.M8"          % "test",
  "junit"                       %   "junit"                     % "4.11"            % "test",
  "com.typesafe"                %%  "scalalogging-slf4j"        % "1.0.1",
  "org.apache.directory.studio" %   "org.apache.commons.codec" 	% "1.8"
)

seq(Revolver.settings: _*)
