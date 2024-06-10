#!/bin/sh
echo "The application will start in ${STARTUP_DELAY}s..." && sleep ${STARTUP_DELAY}
liquibase --defaults-file=liquibase.properties update --url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} --username=${DB_USERNAME} --password=${DB_PASSWORD}
LIQUIBASE_SUCCESS=$?
echo "Liquibase deployment finished with status $LIQUIBASE_SUCCESS, sending signal to terminate proxy"
touch /tmp/signals/finished
exit $LIQUIBASE_SUCCESS