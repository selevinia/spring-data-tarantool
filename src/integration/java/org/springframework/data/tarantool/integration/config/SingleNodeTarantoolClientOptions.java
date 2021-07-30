package org.springframework.data.tarantool.integration.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;

import java.util.List;

@Data
@NoArgsConstructor
public class SingleNodeTarantoolClientOptions implements TarantoolClientOptions {
    private List<String> nodes = List.of("localhost:3303");
    private String userName = "admin";
    private String password = "admin";
    private int connections;
    private int connectTimeout;
    private int readTimeout;
    private int requestTimeout;
    private boolean cluster = false;
    private boolean crudAvailable = false;

}
