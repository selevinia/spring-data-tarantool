package org.springframework.data.tarantool.integration.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.tarantool.config.client.OperationMappingOptions;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomCrudOperationsTarantoolClientOptions implements TarantoolClientOptions, OperationMappingOptions {
    private List<String> nodes = List.of("localhost:3301", "localhost:3302");
    private String userName = "admin";
    private String password = "admin";
    private int connections;
    private int connectTimeout;
    private int readTimeout;
    private int requestTimeout;
    private boolean cluster = true;
    private boolean crudAvailable = true;

    private String schemaFunctionName = "wrapped_get_schema";
    private String deleteFunctionName = "wrapped_delete";
    private String insertFunctionName = "wrapped_insert";
    private String replaceFunctionName = "wrapped_replace";
    private String updateFunctionName = "wrapped_update";
    private String upsertFunctionName = "wrapped_upsert";
    private String selectFunctionName = "wrapped_select";

    @Override
    public String getGetSchemaFunctionName() {
        return schemaFunctionName;
    }
}
