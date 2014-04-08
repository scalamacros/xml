scalaVersion := "2.11.0-RC4"

libraryDependencies ++= Seq(
  // groupID % artifactID % revision
  "org.scala-lang.modules" % "scala-xml_2.11.0-RC4" % "1.0.1",
  "org.scala-lang" % "scala-reflect" % "2.11.0-RC4",
  "org.scalatest" % "scalatest_2.11.0-RC4" % "2.1.3"
)

initialCommands in console := """
  import scala.reflect.runtime.universe._
  import org.scalamacros.xml.runtime._
"""
