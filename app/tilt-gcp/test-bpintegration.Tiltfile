def parse_args(args):
    out = {}

    for arg in args:
        arg["type"](arg["arg"], args=False)

    cfg = config.parse()

    for arg in args:
        key = arg["arg"]
        env_key = arg["env"]

        if key in cfg:
            out[key] = cfg[key]
        elif os.getenv(env_key):
            out[key] = os.getenv(env_key)
        elif "default" in arg:
            out[key] = arg["default"]
        else:
            fail(
                'Missing arg: Either provide tilt with "--'
                + key +
                '" or specify the environment variable "'
                + env_key +
                '"'
            )

    return out


cfg = parse_args([{
    "arg": "to-run",
    "env": "TBM_TO_RUN",
    "type": config.define_string_list,
    "default": []
}, {
    "arg": "tenant",
    "env": "TBM_TENANT",
    "type": config.define_string,
}, {
    "arg": "service-tests",
    "env": "TBM_SERVICE_TESTS",
    "type": config.define_bool,
    "default": False
}, {
    "arg": "proxyMode",
    "env": "TBM_PROXY_MODE",
    "type": config.define_bool,
    "default": False
}, {
    "arg": "quick",
    "env": "TBM_TILT_QUICK",
    "type": config.define_bool,
    "default": False
}, {
    "arg": "registry",
    "env": "TBM_TILT_REGISTRY",
    "type": config.define_string
}, {
    "arg": "nsPrefix",
    "env": "TBM_TILT_NS_PREFIX",
    "type": config.define_string,
    "default": ""
}, {
    "arg": "configDir",
    "env": "TBM_TILT_CONFIG_DIR",
    "type": config.define_string
}, {
    "arg": "debugPort",
    "env": "TBM_TILT_DEBUG_PORT",
    "type": config.define_string
}, {
    "arg": "loginCommand",
    "env": "TBM_TILT_LOGIN_COMMAND",
    "type": config.define_string,
    "default": ""
}, {
    "arg": "context",
    "env": "TBM_TILT_CONTEXT",
    "type": config.define_string,
    "default": ""
}, {
    "arg": "projectName",
    "env": "TBM_PROJECT_NAME",
    "type": config.define_string,
}, {
    "arg": "appName",
    "env": "TBM_APP_NAME",
    "type": config.define_string,
}])

config.set_enabled_resources(cfg["to-run"])
namespace = cfg["nsPrefix"] + cfg["tenant"]

secret_settings(disable_scrub=True)
allow_k8s_contexts(cfg["context"])
default_registry(cfg["registry"])

current_namespace = str(local("kubectl config view --minify --output 'jsonpath={..namespace}'"))
if not current_namespace == namespace:
    fail("Please switch namespace first: 'kubectl config set-context --current --namespace=" + cfg["namespace"] + "'")

application_name = 'api'
mockingValues = ''
overrideTarget = ''

if cfg["loginCommand"]:
    local(cfg["loginCommand"])

#Switch application to point to mock
if cfg["service-tests"] or cfg["proxyMode"]:
    application_name = application_name + '_proxy_target'
    local(
        'cat ../service-tests/ci/mocking-values/values-dev0x.yaml | sed "s/DEV0X/' + cfg["tenant"].upper() + '/" | sed "s/dev0x/' + cfg["tenant"] + '/" > values-mocking-local.yaml')
    mockingValues = '-f  values-mocking-local.yaml '

#Switch Infonova to point to mock
if cfg["proxyMode"]:
    local(
        'cat values/values-dev0x-proxy.yaml | sed "s/DEV0X/' + cfg["tenant"].upper() + '/" | sed "s/dev0x/' + cfg["tenant"] + '/" > values-proxy-local.yaml')
    overrideTarget = '-f  values-proxy-local.yaml '

apiDeps = []

configDisabled = ''
#only deploy config when no service tests
if not cfg["service-tests"]:
    configDeps = []
    config_name = 'config'
    db_name = 'db'

    if os.path.exists("../" + config_name):
        if cfg["proxyMode"]:
            configDeps = ['proxy-mode']
            config_name = config_name + '_proxy_target'
            configDisabled = '--set ' + cfg["appName"] + '.config.enabled=true'

        apiDeps.append(config_name)

        docker_build(
            cfg["registry"] + '/' + cfg["appName"] + '-config',
            "../" + config_name,
            build_args={'IMAGE_REPOSITORY': cfg["registry"]}
        )
        k8s_resource(
            cfg["projectName"] + '-config-' + cfg["tenant"],
            trigger_mode=TRIGGER_MODE_MANUAL,
            resource_deps=configDeps,
            new_name=config_name
        )


    if os.path.exists("../" + db_name):
        apiDeps.append(db_name)

        docker_build(
            cfg["registry"] + '/' + cfg["appName"] + '-db-init',
            "../" + db_name,
            build_args={'IMAGE_REPOSITORY': cfg["registry"]}
        )
        k8s_resource(
            cfg["projectName"] + '-db-init-' + cfg["tenant"],
            trigger_mode=TRIGGER_MODE_MANUAL,
            resource_deps=configDeps,
            new_name=db_name
        )

local(cfg["configDir"] + '/generate.sh ' + cfg["tenant"])

k8s_yaml(
    local('helm dep up ../service-tests/ci/' + cfg["appName"] + '-dev/ >/dev/null && '
          + 'helm template ' + cfg["projectName"] + '-tilt ../service-tests/ci/' + cfg["appName"] + '-dev/ '
          + '-f ' + cfg["configDir"] + '/dev/values/values-dev.yaml '
          + '-f ' + cfg["configDir"] + '/dev/generated/values-generated.yaml '
          + '-f secrets://' + cfg["configDir"] + '/dev/secrets/secrets-dev.yaml '
          + '--set ' + cfg["appName"] + '.app.debug=true '
          + mockingValues
          + overrideTarget
          + configDisabled
          + '--namespace=' + namespace
          )
)

k8s_resource(
    cfg["projectName"] + '-api-' + cfg["tenant"],
    trigger_mode=TRIGGER_MODE_MANUAL,
    resource_deps=apiDeps,
    new_name=application_name,
    port_forwards=[cfg["debugPort"]]
)

if cfg["quick"]:
    custom_build(
        cfg["registry"] + '/' + cfg["appName"] + '',
        'cd .. && mvn install -Pdocker, -Dimage=$EXPECTED_REF -DIMAGE_REPOSITORY=' + cfg["registry"] + ' --projects app -DskipTests ',
        deps=['../' + cfg["appName"] + '/app/src'],
        skips_local_docker=True, )
else:
    custom_build(
        cfg["registry"] + '/' + cfg["appName"] + '',
        'cd .. && mvn install -Pdocker, -Dimage=$EXPECTED_REF -DIMAGE_REPOSITORY=' + cfg["registry"] + ' --projects app --also-make ',
        deps=['../' + cfg["appName"] + '/app/src'],
        skips_local_docker=True
    )
