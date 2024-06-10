#!/bin/sh
DEBUG_OPTIONS=""
if [ -n "${DEBUG_PORT}" ];
then
   echo "Enabling debug on port ${DEBUG_PORT}"
   DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"
fi

echo "The application will start in ${JHIPSTER_SLEEP}s..." && sleep ${JHIPSTER_SLEEP}
exec java ${JAVA_OPTS} \
-noverify ${DEBUG_OPTIONS} \
-XX:+AlwaysPreTouch \
-Djava.security.egd=file:/dev/./urandom \
-cp /app/resources/:/app/classes/:/app/libs/* \
"com.bearingpoint.beyond.test-bpintegration.Application"  \
"$@" \
--logging.config=file:/app/config/logback.xml
