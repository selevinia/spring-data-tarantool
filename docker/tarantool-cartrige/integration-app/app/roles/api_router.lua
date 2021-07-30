local cartridge_pool = require('cartridge.pool')
local cartridge_rpc = require('cartridge.rpc')
local cartridge = require('cartridge')

local function get_schema()
    for _, instance_uri in pairs(cartridge_rpc.get_candidates('app.roles.api_storage', { leader_only = true })) do
        local conn = cartridge_pool.connect(instance_uri)
        return conn:call('ddl.get_schema', {})
    end
end

function wrapped_get_schema()
    return get_schema()
end

function wrapped_delete(space_name, key, opts)
    return crud.delete(space_name, key, opts)
end

function wrapped_insert(space_name, tuple, opts)
    return crud.insert(space_name, tuple, opts)
end

function wrapped_replace(space_name, tuple, opts)
    return crud.replace(space_name, tuple, opts)
end

function wrapped_select(space_name, conditions, opts)
    return crud.select(space_name, conditions, opts)
end

function wrapped_update(space_name, key, operations, opts)
    return crud.update(space_name, key, operations, opts)
end

function wrapped_upsert(space_name, tuple, operations, opts)
    return crud.upsert(space_name, tuple, operations, opts)
end

local function get_storage_lieder()
    local lieder, err = cartridge.rpc_get_candidates('app.roles.api_storage', { leader_only = true })
    if err ~= nil then
        return nil, err
    end
    return lieder
end

function raise_error()
    error("Error from raise_error function")
end

function get_error()
    return nil, "Error from get_error function"
end

function get_nothing()
end

function get_nil()
    return nil
end

function upload_users(users)
    local results = 0
    local failures = {}
    for _, user in pairs(users) do
        local ok, obj, err = pcall(crud.replace_object, 'users', user)
        if ok and err == nil then
            results = results + 1
        else
            failures[#failures + 1] = ok and tostring(err) or tostring(obj)
        end
    end
    if #failures > 0 then
        return nil, "Failed to update: " .. table.concat(failures, ', ')
    end
    return results;
end

function find_users_by_last_name(name)
    local users = {}
    local users_by_storage, err = cartridge_pool.map_call('find_users_by_last_name', { name }, { uri_list = get_storage_lieder() })
    if err then
        return nil, err
    end
    for _, current in pairs(users_by_storage) do
        for _, user in pairs(current) do
            table.insert(users, user)
        end
    end
    return users
end

function count_users_by_last_name(name)
    local cnt = 0
    local cnt_by_storage, err = cartridge_pool.map_call('count_users_by_last_name', { name }, { uri_list = get_storage_lieder() })
    if err then
        return nil, err
    end
    for _, current in pairs(cnt_by_storage) do
        cnt = cnt + current
    end
    return cnt
end

function find_user_by_user(user)
    local users = {}
    local users_by_storage, err = cartridge_pool.map_call('find_user_by_user', { user }, { uri_list = get_storage_lieder() })
    if err then
        return nil, err
    end
    for _, current in pairs(users_by_storage) do
        for _, user in pairs(current) do
            table.insert(users, user)
        end
    end
    return users
end

function get_articles()
    local articles = {}
    local articles_by_storage, err = cartridge_pool.map_call('get_articles', { }, { uri_list = get_storage_lieder() })
    if err then
        return nil, err
    end
    for _, current in pairs(articles_by_storage) do
        for _, article in pairs(current) do
            table.insert(articles, article)
        end
    end
    return articles
end

function get_article_by_entity(article)
    local articles = {}
    local articles_by_storage, err = cartridge_pool.map_call('get_article_by_entity', { article }, { uri_list = get_storage_lieder() })
    if err then
        return nil, err
    end
    for _, current in pairs(articles_by_storage) do
        for _, article in pairs(current) do
            table.insert(articles, article)
        end
    end
    return articles
end

function get_article_element_by_id(article_id)
    local article = crud.get('articles', article_id)
    local result = crud.unflatten_rows(article.rows, article.metadata)[1]

    local user = crud.get('users', result['user_id'])
    local comments = crud.select('comments', { { '=', 'article_id', article_id } })

    result["user"] = crud.unflatten_rows(user.rows, user.metadata)[1]
    result["comments"] = {}
    for i, _ in pairs(comments.rows) do
        result["comments"][i] = crud.unflatten_rows(comments.rows, comments.metadata)[i]
    end

    return result
end

function get_articles_by_user(user)
    local articles = {}
    local articles_by_storage, err = cartridge_pool.map_call('get_articles_by_user', { user }, { uri_list = get_storage_lieder() })
    if err then
        return nil, err
    end
    for _, current in pairs(articles_by_storage) do
        for _, article in pairs(current) do
            table.insert(articles, article)
        end
    end
    return articles
end

function get_articles_by_user_id(user_id)
    local articles = {}
    local articles_by_storage, err = cartridge_pool.map_call('get_articles_by_user_id', { user_id }, { uri_list = get_storage_lieder() })
    if err then
        return nil, err
    end
    for _, current in pairs(articles_by_storage) do
        for _, article in pairs(current) do
            table.insert(articles, article)
        end
    end
    return articles
end

function count_articles_by_user_id(user_id)
    local cnt = 0
    local cnt_by_storage, err = cartridge_pool.map_call('count_articles_by_user_id', { user_id }, { uri_list = get_storage_lieder() })
    if err then
        return nil, err
    end
    for _, current in pairs(cnt_by_storage) do
        cnt = cnt + current
    end
    return cnt
end

local function init(opts) -- luacheck: no unused args
    rawset(_G, 'ddl', { get_schema = get_schema })
    return true
end

return {
    role_name = 'app.roles.api_router',
    init = init,
    dependencies = {
        'cartridge.roles.crud-router'
    }
}
