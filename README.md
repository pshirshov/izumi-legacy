A simple Scala toolkit for fast evolving advanced applications.

## Publishing

    $ sbt clean publish
    $ sbt updateBugfix
    $ git checkout HEAD~1
    $ sbt clean publish sonatypeRelease
    $ git checkout master