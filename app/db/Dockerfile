ARG IMAGE_REPOSITORY=northamerica-northeast1-docker.pkg.dev/cto-bpintegration-np-4ea75a/applications/sales-bp-integration
FROM $IMAGE_REPOSITORY/liquibase/liquibase:4.4.3

ADD liquibase /liquibase

ENV DB_PORT=5432
ENV DB_NAME=postgres
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=product1
ENV DB_HOST=localhost
WORKDIR /liquibase/changelog
ADD entrypoint.sh /
ENTRYPOINT /entrypoint.sh
