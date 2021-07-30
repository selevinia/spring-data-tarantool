local articles = {}

function articles.init()
    local space = box.schema.space.create("articles", { if_not_exists = true })
    space:format({
        { name = "id", type = "uuid" },
        { name = "article_name", type = "string" },
        { name = "slug", type = "string", is_nullable = true },
        { name = "publish_date", type = "unsigned" },
        { name = "user_id", type = "uuid" },
        { name = "tags", type = "array", is_nullable = true },
        { name = "likes", type = "unsigned", is_nullable = true },
    })

    space:create_index("primary", { parts = { { field = "id" } },
                                    if_not_exists = true })
    space:create_index("article_name", { parts = { { field = "article_name" } },
                                         unique = false,
                                         if_not_exists = true })
    space:create_index("user_id", { parts = { { field = "user_id" } },
                                    unique = false,
                                    if_not_exists = true })
end

return articles