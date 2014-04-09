import sbt._
import Keys._
import com.typesafe.sbt.pgp.PgpKeys._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys._

object build extends Build {
  lazy val sharedSettings = Defaults.defaultSettings ++ Seq(
    scalaVersion := "2.11.0-RC4",
    crossVersion := CrossVersion.full,
    version := "1.0.0-SNAPSHOT",
    organization := "org.scalamacros",
    description := "Lifting/unlifting support for XML literals",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    publishMavenStyle := true,
    publishArtifact in Compile := false,
    publishArtifact in Test := false,
    scalacOptions ++= Seq("-deprecation", "-feature", "-optimise"),
    parallelExecution in Test := false, // hello, reflection sync!!
    logBuffered := false,
    scalaHome := {
      val scalaHome = System.getProperty("xml.scala.home")
      if (scalaHome != null) {
        println(s"Going for custom scala home at $scalaHome")
        Some(file(scalaHome))
      } else None
    },
    publishMavenStyle := true,
    publishOnlyWhenOnMaster := publishOnlyWhenOnMasterImpl.value,
    publishTo <<= version { v: String =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomIncludeRepository := { x => false },
    pomExtra := (
      <url>https://github.com/scalareflect/xml</url>
      <inceptionYear>2014</inceptionYear>
      <licenses>
        <license>
          <name>BSD-like</name>
          <url>http://www.scala-lang.org/downloads/license.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git://github.com/scalareflect/xml.git</url>
        <connection>scm:git:git://github.com/scalareflect/xml.git</connection>
      </scm>
      <issueManagement>
        <system>GitHub</system>
        <url>https://github.com/scalareflect/xml/issues</url>
      </issueManagement>
    )
  )

  // http://stackoverflow.com/questions/20665007/how-to-publish-only-when-on-master-branch-under-travis-and-sbt-0-13
  lazy val publishOnlyWhenOnMaster = taskKey[Unit]("publish task for Travis (don't publish when building pull requests, only publish when the build is triggered by merge into master)")
  def publishOnlyWhenOnMasterImpl = Def.taskDyn {
    import scala.util.Try
    val travis   = Try(sys.env("TRAVIS")).getOrElse("false") == "true"
    val pr       = Try(sys.env("TRAVIS_PULL_REQUEST")).getOrElse("false") != "false"
    val branch   = Try(sys.env("TRAVIS_BRANCH")).getOrElse("??")
    val snapshot = version.value.trim.endsWith("SNAPSHOT")
    (travis, pr, branch, snapshot) match {
      case (true, false, "master", true) => publish
      case _                             => Def.task ()
    }
  }

  lazy val publishableSettings = sharedSettings ++ Seq(
    publishArtifact in Compile := true,
    publishArtifact in Test := false,
    credentials ++= {
      val mavenSettingsFile = System.getProperty("maven.settings.file")
      if (mavenSettingsFile != null) {
        println("Loading Sonatype credentials from " + mavenSettingsFile)
        try {
          import scala.xml._
          val settings = XML.loadFile(mavenSettingsFile)
          def readServerConfig(key: String) = (settings \\ "settings" \\ "servers" \\ "server" \\ key).head.text
          Some(Credentials(
            "Sonatype Nexus Repository Manager",
            "oss.sonatype.org",
            readServerConfig("username"),
            readServerConfig("password")
          ))
        } catch {
          case ex: Exception =>
            println("Failed to load Maven settings from " + mavenSettingsFile + ": " + ex)
            None
        }
      } else {
        for {
          realm <- sys.env.get("SCALAREFLECT_MAVEN_REALM")
          domain <- sys.env.get("SCALAREFLECT_MAVEN_DOMAIN")
          user <- sys.env.get("SCALAREFLECT_MAVEN_USER")
          password <- sys.env.get("SCALAREFLECT_MAVEN_PASSWORD")
        } yield {
          println("Loading Sonatype credentials from environment variables")
          Credentials(realm, domain, user, password)
        }
      }
    }.toList
  )

  lazy val root = Project(
    id = "root",
    base = file("root")
  ) settings (
    sharedSettings : _*
  ) settings (
    console in Compile := (console in xml in Compile).value,
    test in Test := (test in tests in Test).value,
    packagedArtifacts := Map.empty
  ) aggregate (xml, tests)

  lazy val xml = Project(
    id   = "xml",
    base = file("xml")
  ) settings (
    publishableSettings: _*
  ) settings (
    initialCommands in console := """
      import scala.reflect.runtime.universe._
      import org.scalamacros.xml.RuntimeLiftables._
    """,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml"     % "1.0.1",
      "org.scala-lang"         %  "scala-reflect" % "2.11.0-RC4"
    ),
    crossVersion := CrossVersion.binary
    // TODO: uncomment this when M1 is published
    // previousArtifact := Some("org.scalamacros" %% "xml" % "1.0.0-M1"),
    // Keys.`package` in Compile := {
    //   if (findBinaryIssues.value.nonEmpty) throw new Exception("binary incompatible with " + previousArtifact.value)
    //   (Keys.`package` in Compile).value
    // },
    // packagedArtifact in Compile in packageBin := {
    //   if (findBinaryIssues.value.nonEmpty) throw new Exception("binary incompatible with " + previousArtifact.value)
    //   (packagedArtifact in Compile in packageBin).value
    // }
  )

  lazy val tests = Project(
    id   = "tests",
    base = file("tests")
  ) settings (
    sharedSettings: _*
  ) settings (
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "test",
    packagedArtifacts := Map.empty
  ) dependsOn (xml)
}
