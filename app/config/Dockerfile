ARG IMAGE_REPOSITORY=${TBM_TILT_REGISTRY:-northamerica-northeast1-docker.pkg.dev/cto-bpintegration-np-4ea75a/applications/sales-bp-integration}
FROM ${IMAGE_REPOSITORY:?}/config-upload-tool:3.1.0

ADD config /opt/config
ADD customOperations /opt/config-upload-tool/processor/operations

CMD ["/opt/config/tool-config.yaml", "/opt/config/steps-config.yaml", "/opt/config/variables.yaml"]