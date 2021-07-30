package org.springframework.data.tarantool.integration.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;

import java.util.List;

@Data
@NoArgsConstructor
public class CartridgeTarantoolClientOptions implements TarantoolClientOptions {
    private List<String> nodes = List.of("localhost:3301", "localhost:3302");
    private String userName = "admin";
    private String password = "admin";
    private int connections;
    private int connectTimeout;
    private int readTimeout;
    private int requestTimeout;
    private boolean cluster = true;
    private boolean crudAvailable = true;

}
