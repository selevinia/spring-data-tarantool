package = 'integration-app'
version = 'scm-1'
source  = {
    url = '/dev/null',
}
-- Put any modules your app depends on here
dependencies = {
    'tarantool',
    'lua >= 5.1',
    'checks == 3.1.0-1',
    'cartridge == 2.6.0-1',
    'metrics == 0.8.0-1',
    'cartridge-cli-extensions == 1.1.1-1',
    'ddl == 1.4.0',
    'crud == 0.8.0',
}
build = {
    type = 'none';
}
