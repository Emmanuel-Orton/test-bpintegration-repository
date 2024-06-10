#!/bin/sh

curlfail(){
  echo "Uploading in file content of $2 to $1"
  CONTENT=$(envsubst < $2)

  OUTPUT_FILE=$(mktemp)

  HTTP_CODE=$(curl -s -X POST --fail-with-body --output "$OUTPUT_FILE" --data "$CONTENT" \
    http://$1.dev.tel-dev.be-delivery.com/mocking/__admin/mappings --write-out "%{http_code}")

  if [ "${HTTP_CODE}" -lt 200 ] || [ "${HTTP_CODE}" -ge 299 ]; then
      echo "Received ${HTTP_CODE} when posting proxy config"
      echo "-------------"
      >&2 cat "$OUTPUT_FILE"
      rm "${OUTPUT_FILE}"
      exit 22
  fi
  rm "${OUTPUT_FILE}"
}

export target="$1"

curlfail "$target" payloads/adminProxy.json
curlfail "$target" payloads/infonovaProxy.json
curlfail "$target" payloads/applicationProxy.json
