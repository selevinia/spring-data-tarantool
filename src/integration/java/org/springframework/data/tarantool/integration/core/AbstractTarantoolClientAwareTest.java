package org.springframework.data.tarantool.integration.core;

import io.tarantool.driver.RetryingTarantoolTupleClient;
import io.tarantool.driver.TarantoolRequestRetryPolicies.InfiniteRetryPolicyFactory;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.core.TarantoolTemplate;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;

import java.util.function.Function;

import static org.springframework.data.tarantool.integration.config.TestConfigProvider.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTarantoolClientAwareTest {
    protected TarantoolTemplate tarantoolTemplate;

    public abstract TarantoolClientOptions getOptions();

    @BeforeAll
    void setUp() {
        TarantoolMappingContext mappingContext = mappingContext();
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = clientFactory(getOptions()).createClient();
        InfiniteRetryPolicyFactory<Function<Throwable, Boolean>> retryPolicyFactory = new InfiniteRetryPolicyFactory<>(1, 1, t -> false);
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> retryingClient = new RetryingTarantoolTupleClient(client, retryPolicyFactory);
        tarantoolTemplate = new TarantoolTemplate(retryingClient, converter(mappingContext), exceptionTranslator());
    }

}