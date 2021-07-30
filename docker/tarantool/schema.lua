local schema = {}

function schema.init()
    box.once('schema', function()
        require("schema/users").init()
        require("schema/articles").init()
        require("schema/translated_articles").init()
        require("schema/comments").init()
    end)
end

return schema