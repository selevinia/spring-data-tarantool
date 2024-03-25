package = 'integration-app'
version = 'scm-1'
source  = {
    url = '/dev/null',
}
-- Put any modules your app depends on here
dependencies = {
    'tarantool',
    'lua >= 5.1',
    'cartridge == 2.7.3-1',
    'cartridge-cli-extensions == 1.1.1-1',
    'ddl == 1.6.5',
    'crud == 0.11.1-1',
    'migrations == 0.4.2-1',
}
build = {
    type = 'none';
}
