A simple Scala toolkit for fast evolving advanced applications.

## Publishing

The most convenient workflow:

1. `sbt updateBugfix`
2. `git reset HEAD~1`
3. `sbt -Dpb.strict-version=false clean "very publish" sonatypeRelease`
4. `git checkout master`
