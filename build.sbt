lazy val buildSettings = List(
  organization := "com.adelbertc",
  licenses += ("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
)

lazy val commonSettings = List(
  scalacOptions ++= List(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  ),
  resolvers ++= List(
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= List(
    compilerPlugin("org.spire-math" %% "kind-projector"   % "0.8.0"),
    compilerPlugin("com.milessabin" %  "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)
  )
)

lazy val scalaSandboxSettings = buildSettings ++ commonSettings

lazy val taglessStackSafety =
  project.in(file("tagless-stacksafety")).
  settings(name := "tagless-stacksafety").
  settings(scalaSandboxSettings).
  settings(
    scalaVersion := "2.11.8",
    scalacOptions in compile ++= List(
      "-Ywarn-unused-import"
    ),
    libraryDependencies ++= List(
      "org.typelevel" %% "cats" % "0.7.0-SNAPSHOT"
    )
  ).
  enablePlugins(JmhPlugin)

lazy val rankNClassy =
  project.in(file("ranknclassy")).
  settings(name := "ranknclassy").
  settings(scalaSandboxSettings).
  settings(
    scalaVersion := "2.11.8",
    scalacOptions in compile ++= List(
      "-Ywarn-unused-import"
    ),
    libraryDependencies ++= List(
      "org.typelevel" %% "cats"       % "0.6.1",
      "io.circe"      %% "circe-core" % "0.5.0-M2"
    )
  )
