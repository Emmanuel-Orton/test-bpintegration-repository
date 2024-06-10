#!/bin/sh

OUTPUT_FILE=$(mktemp)
HTTP_CODE=$(curl -X POST -s --fail-with-body --output "$OUTPUT_FILE"\
 http://$1.dev.tel-dev.be-delivery.com/mocking/__admin/reset --write-out "%{http_code}")

if  [ "${HTTP_CODE}" -lt 200 ] || [ "${HTTP_CODE}" -ge 299 ]; then
  # Ignore 404 for proxy config reset
  if [ "${HTTP_CODE}" != 404 ]; then
    echo "Received ${HTTP_CODE} while resetting proxy config"
    echo "-------------"
    >&2 cat "$OUTPUT_FILE"
    rm "${OUTPUT_FILE}"
    #Error code from curl if error happens
    return 22
  fi
fi

rm "${OUTPUT_FILE}"
