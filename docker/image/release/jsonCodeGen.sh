#!/bin/sh

scriptPos=${0%/*}

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    # IBM's JDK on AIX uses strange locations for the executables
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    elif [ -x "$JAVA_HOME/jre/bin/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/bin/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

args=
for arg in "$@";
do
  args="$args \"$arg\""
done

if [ -d $scriptPos/lib ]; then
    JSONCODEGEN_LIB_DIR=$scriptPos/lib
    LOGDIR=$scriptPos/conf
else
    JSONCODEGEN_LIB_DIR="$JSONCODEGEN_HOME/lib"
    LOGDIR="$JSONCODEGEN_HOME/conf"
fi

$JAVACMD -cp "$JSONCODEGEN_LIB_DIR/*" "-Dlogback.configurationFile=$scriptPos/conf/logback.xml\" de.lisaplus.atlas.DoCodeGen $args