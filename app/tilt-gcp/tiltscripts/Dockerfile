FROM alpine:3.14.1

RUN apk --no-cache add curl gettext
RUN mkdir /opt/app
WORKDIR /opt/app

COPY scripts/*.sh /opt/app/
COPY payloads /opt/app/payloads/
