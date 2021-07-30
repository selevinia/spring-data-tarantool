local users = {}

function users.init()
    local space = box.schema.space.create("users", { if_not_exists = true })
    space:format({
        { name = "id", type = "uuid" },
        { name = "first_name", type = "string" },
        { name = "last_name", type = "string" },
        { name = "email", type = "string", is_nullable = true },
        { name = "birth_date", type = "unsigned" },
        { name = "address", type = "any", is_nullable = true },
        { name = "age", type = "unsigned" },
        { name = "active", type = "boolean" },
        { name = "version", type = "unsigned" },
    })

    space:create_index("primary", { parts = { { field = "id" } },
                                    if_not_exists = true })
    space:create_index("last_name", { parts = { { field = "last_name" } },
                                      unique = false,
                                      if_not_exists = true })
    space:create_index("first_name_last_name", { parts = { { field = "first_name" }, { field = "last_name" } },
                                                 unique = false,
                                                 if_not_exists = true })
    space:create_index("birth_date", { parts = { { field = "birth_date" } },
                                       unique = false,
                                       if_not_exists = true })
    space:create_index("age", { parts = { { field = "age" } },
                                unique = false,
                                if_not_exists = true })
    space:create_index("last_name_age", { parts = { { field = "last_name" }, { field = "age" } },
                                          unique = false,
                                          if_not_exists = true })
    space:create_index("active", { parts = { { field = "active" } },
                                   unique = false,
                                   if_not_exists = true })
    space:create_index("last_name_active", { parts = { { field = "last_name" }, { field = "active" } },
                                             unique = false,
                                             if_not_exists = true })
end

return users