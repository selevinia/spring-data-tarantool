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

function get_articles()
    return box.space.articles:select()
end

function get_article_by_entity(article)
    return { box.space.articles:get(article.id) }
end

function get_article_element_by_id(article_id)
    local article = box.space.articles:get(article_id)
    local user = box.space.users:get(article.user_id)
    local comments = box.space.comments.index.article_id:select(article_id)

    local result = article:tomap({ names_only = true })
    result["user"] = user:tomap({ names_only = true })
    result["comments"] = {}
    for i, comment in pairs(comments) do
        result["comments"][i] = comment:tomap({ names_only = true })
    end

    return result
end

function get_articles_by_user(user)
    return box.space.articles.index.user_id:select(user.id)
end

function get_articles_by_user_id(user_id)
    return box.space.articles.index.user_id:select(user_id)
end

function count_articles_by_user_id(user_id)
    return box.space.articles.index.user_id:count(user_id)
end

function find_users_by_last_name(name)
    return box.space.users.index.last_name:select(name)
end

function count_users_by_last_name(name)
    return box.space.users.index.last_name:count(name)
end

function find_user_by_user(user)
    return { box.space.users:get(user.id) }
end