tarantoolctl connect admin:admin@127.0.0.1:3301 << EOF
cartridge = require('cartridge')
replicasets = { {
                    alias = 'router1',
                    roles = { 'crud-router', 'vshard-router', 'app.roles.api_router' },
                    join_servers = { { uri = 'tarantool-router-1:3301' } }
                }, {
                    alias = 'router2',
                    roles = { 'crud-router', 'vshard-router', 'app.roles.api_router' },
                    join_servers = { { uri = 'tarantool-router-2:3301' } }
                }, {
                    alias = 'storage1',
                    roles = { 'crud-storage', 'vshard-storage', 'app.roles.api_storage' },
                    join_servers = { { uri = 'tarantool-s1-master:3301' },
                                     { uri = 'tarantool-s1-replica:3301' } }
                }, {
                    alias = 'storage2',
                    roles = { 'crud-storage', 'vshard-storage', 'app.roles.api_storage' },
                    join_servers = { { uri = 'tarantool-s2-master:3301' },
                                     { uri = 'tarantool-s2-replica:3301' } }
                } }
cartridge.admin_edit_topology({ replicasets = replicasets })
EOF

sleep 1

tarantoolctl connect admin:admin@127.0.0.1:3301 << EOFR
cartridge = require('cartridge')

cartridge.admin_bootstrap_vshard()
EOFR
