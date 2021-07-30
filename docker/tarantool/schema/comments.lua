local comments = {}

function comments.init()
    local space = box.schema.space.create("comments", { if_not_exists = true })
    space:format({
        { name = "id", type = "uuid" },
        { name = "article_id", type = "uuid" },
        { name = "user_id", type = "uuid" },
        { name = "value", type = "string" },
        { name = "likes", type = "unsigned", is_nullable = true },
    })

    space:create_index("primary", { parts = { { field = "id" } },
                                    if_not_exists = true })
    space:create_index("article_id", { parts = { { field = "article_id" } },
                                       unique = false,
                                       if_not_exists = true })
    space:create_index("user_id", { parts = { { field = "user_id" } },
                                    unique = false,
                                    if_not_exists = true })
end

return comments