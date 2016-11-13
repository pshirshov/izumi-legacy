import izumitk.Dependencies._


libraryDependencies ++=
    C.akka ++
    T.akka_test ++
    Seq(
      C.jwt
      , C.halbuilder
    )


