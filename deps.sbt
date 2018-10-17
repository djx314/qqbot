val circeVersion = "0.10.0"

libraryDependencies += ws
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.2"
libraryDependencies += "commons-io"        % "commons-io"              % "2.5"
libraryDependencies += "com.dripower"      %% "play-circe"             % "2610.0"
libraryDependencies += "io.circe"          %% "circe-optics"           % circeVersion
libraryDependencies += "io.circe"          %% "circe-generic-extras"   % circeVersion
libraryDependencies += "net.scalax"        %% "asuna-mapper"           % "0.0.2-SNAP20181003.1"
libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick"          % "3.2.3"
  , "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3"
  , "com.typesafe.play"  %% "play-slick"     % "3.0.1"
  , "com.h2database"     % "h2"              % "1.4.197"
)

libraryDependencies += "net.coobird" % "thumbnailator" % "0.4.8"

libraryDependencies ++= Seq(
    "com.softwaremill.macwire" %% "macros"     % "2.3.0" % "provided"
  , "com.softwaremill.macwire" %% "macrosakka" % "2.3.0" % "provided"
  , "com.softwaremill.macwire" %% "util"       % "2.3.0"
  , "com.softwaremill.macwire" %% "proxy"      % "2.3.0"
)

libraryDependencies ++= Seq(
    "org.webjars.bower" % "requirejs"      % "2.3.3"
  , "org.webjars.bower" % "requirejs-text" % "2.0.15"
)
