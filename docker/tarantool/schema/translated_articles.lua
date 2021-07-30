local translated_articles = {}

function translated_articles.init()
    local space = box.schema.space.create("translated_articles", { if_not_exists = true })
    space:format({
        { name = "article_id", type = "uuid" },
        { name = "locale", type = "string" },
        { name = "article_name", type = "string" },
        { name = "article_text", type = "string", is_nullable = true },
    })

    space:create_index("primary", { parts = { { field = "article_id" }, { field = "locale" } },
                                    if_not_exists = true })
end

return translated_articles