#!/bin/sh
# Minimal Maven Wrapper launcher
set -e
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Missing $WRAPPER_JAR" >&2
  exit 1
fi

exec java -classpath "$WRAPPER_JAR" "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" $WRAPPER_LAUNCHER "$@"
