#!/bin/sh

bin=`dirname "$0"`
APP_HOME=`cd "$bin"; pwd`

if [ $# = 0 ]; then
   echo "Usage: $0 COMMAND <args>"
   exit 1
fi

MAIN_CLASS=$1
shift

CLASSPATH=$APP_HOME/FriendsRecommend.jar:$APP_HOME
# add libs to CLASSPATH
for f in $APP_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done
# add exts to CLASSPATH
for f in $APP_HOME/ext/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done
#echo $CLASSPATH

if [ "$JAVA_HOME" = "" ]; then
  JAVA_BIN=java
else
  JAVA_BIN=$JAVA_HOME/bin/java
fi

JAVA_OPTIONS="-Xmx2048M -XX:+UseConcMarkSweepGC"
#MAIN_CLASS=com.duowan.yy.userbehavior.UserBehaviorLogConverter3

#echo $JAVA_BIN $JAVA_OPTIONS -cp $CLASSPATH $MAIN_CLASS $*
$JAVA_BIN $JAVA_OPTIONS -cp "$CLASSPATH" $MAIN_CLASS "$@"
