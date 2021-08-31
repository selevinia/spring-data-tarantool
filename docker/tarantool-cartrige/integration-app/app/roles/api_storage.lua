local function init_space()
    -- Articles
    local articles = box.schema.space.create("articles", { if_not_exists = true })
    articles:format({
        { name = "id", type = "uuid" },
        { name = "bucket_id", type = "unsigned" },
        { name = "article_name", type = "string" },
        { name = "slug", type = "string", is_nullable = true },
        { name = "publish_date", type = "unsigned" },
        { name = "user_id", type = "uuid" },
        { name = "tags", type = "array", is_nullable = true },
        { name = "likes", type = "unsigned", is_nullable = true },
    })

    articles:create_index("primary", { parts = { { field = "id" } },
                                       if_not_exists = true })
    articles:create_index("bucket_id", { parts = { { field = "bucket_id" } },
                                         unique = false,
                                         if_not_exists = true })
    articles:create_index("article_name", { parts = { { field = "article_name" } },
                                            unique = false,
                                            if_not_exists = true })
    articles:create_index("user_id", { parts = { { field = "user_id" } },
                                       unique = false,
                                       if_not_exists = true })

    -- Users
    local users = box.schema.space.create("users", { if_not_exists = true })
    users:format({
        { name = "id", type = "uuid" },
        { name = "bucket_id", type = "unsigned" },
        { name = "first_name", type = "string" },
        { name = "last_name", type = "string" },
        { name = "email", type = "string", is_nullable = true },
        { name = "birth_date", type = "unsigned" },
        { name = "age", type = "unsigned" },
        { name = "active", type = "boolean" },
        { name = "address", type = "any", is_nullable = true },
        { name = "version", type = "unsigned" },
    })

    users:create_index("primary", { parts = { { field = "id" } },
                                    if_not_exists = true })
    users:create_index("bucket_id", { parts = { { field = "bucket_id" } },
                                      unique = false,
                                      if_not_exists = true })
    users:create_index("last_name", { parts = { { field = "last_name" } },
                                      unique = false,
                                      if_not_exists = true })
    users:create_index("first_name_last_name", { parts = { { field = "first_name" }, { field = "last_name" } },
                                                 unique = false,
                                                 if_not_exists = true })
    users:create_index("birth_date", { parts = { { field = "birth_date" } },
                                       unique = false,
                                       if_not_exists = true })
    users:create_index("age", { parts = { { field = "age" } },
                                unique = false,
                                if_not_exists = true })
    users:create_index("last_name_age", { parts = { { field = "last_name" }, { field = "age" } },
                                          unique = false,
                                          if_not_exists = true })
    users:create_index("active", { parts = { { field = "active" } },
                                   unique = false,
                                   if_not_exists = true })
    users:create_index("last_name_active", { parts = { { field = "last_name" }, { field = "active" } },
                                             unique = false,
                                             if_not_exists = true })

    -- Comments
    local comments = box.schema.space.create("comments", { if_not_exists = true })
    comments:format({
        { name = "id", type = "uuid" },
        { name = "bucket_id", type = "unsigned" },
        { name = "article_id", type = "uuid" },
        { name = "user_id", type = "uuid" },
        { name = "value", type = "string" },
        { name = "likes", type = "unsigned", is_nullable = true },
    })

    comments:create_index("primary", { parts = { { field = "id" } },
                                       if_not_exists = true })
    comments:create_index("bucket_id", { parts = { { field = "bucket_id" } },
                                         unique = false,
                                         if_not_exists = true })
    comments:create_index("article_id", { parts = { { field = "article_id" } },
                                          unique = false,
                                          if_not_exists = true })
    comments:create_index("user_id", { parts = { { field = "user_id" } },
                                       unique = false,
                                       if_not_exists = true })

    -- Translated articles
    local translated = box.schema.space.create("translated_articles", { if_not_exists = true })
    translated:format({
        { name = "article_id", type = "uuid" },
        { name = "bucket_id", type = "unsigned" },
        { name = "locale", type = "string" },
        { name = "article_name", type = "string" },
        { name = "article_text", type = "string", is_nullable = true },
    })

    translated:create_index("primary", { parts = { { field = "article_id" }, { field = "locale" } },
                                         if_not_exists = true })

    translated:create_index("bucket_id", { parts = { { field = "bucket_id" } },
                                           unique = false,
                                           if_not_exists = true })

    local cache = box.schema.space.create("integration_test", { if_not_exists = true })
    cache:format({
        { name = "key", type = "scalar"},
        { name = "value", type = "any"},
        { name = "expiry_time", type = "unsigned"},
        { name = "bucket_id", type = "unsigned" },
    })

    cache:create_index("primary", { parts = { { field = "key" } },
                                       if_not_exists = true })

    cache:create_index("bucket_id", { parts = { { field = "bucket_id" } },
                                           unique = false,
                                           if_not_exists = true })

    cache:create_index("expiry_time", { parts = { { field = "expiry_time" } },
                                           unique = false,
                                           if_not_exists = true })
end

local function find_users_by_last_name(name)
    return box.space.users.index.last_name:select(name)
end

local function count_users_by_last_name(name)
    return box.space.users.index.last_name:count(name)
end

local function find_user_by_user(user)
    return { box.space.users:get(user.id) }
end

local function get_articles()
    return box.space.articles:select()
end

local function get_article_by_entity(article)
    return { box.space.articles:get(article.id) }
end

local function get_articles_by_user(user)
    return box.space.articles.index.user_id:select(user.id)
end

local function get_articles_by_user_id(user_id)
    return box.space.articles.index.user_id:select(user_id)
end

local function count_articles_by_user_id(user_id)
    return box.space.articles.index.user_id:count(user_id)
end

local function init(opts)
    if opts.is_master then
        init_space()
    end

    rawset(_G, 'find_users_by_last_name', find_users_by_last_name)
    rawset(_G, 'find_user_by_user', find_user_by_user)
    rawset(_G, 'count_users_by_last_name', count_users_by_last_name)
    rawset(_G, 'get_articles', get_articles)
    rawset(_G, 'get_article_by_entity', get_article_by_entity)
    rawset(_G, 'get_articles_by_user', get_articles_by_user)
    rawset(_G, 'get_articles_by_user_id', get_articles_by_user_id)
    rawset(_G, 'count_articles_by_user_id', count_articles_by_user_id)
    rawset(_G, "ddl", { get_schema = require("ddl").get_schema })
    return true
end

return {
    role_name = "app.roles.api_storage",
    init = init,
    utils = {
        find_users_by_last_name = find_users_by_last_name,
        find_user_by_user = find_user_by_user,
    },
    dependencies = { "cartridge.roles.crud-storage" },
}
