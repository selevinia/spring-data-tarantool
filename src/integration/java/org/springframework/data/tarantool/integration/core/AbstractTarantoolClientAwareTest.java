package org.springframework.data.tarantool.integration.core;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.retry.TarantoolRequestRetryPolicies;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.core.RetryingTarantoolTupleClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.core.TarantoolTemplate;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;

import java.util.function.Predicate;

import static org.springframework.data.tarantool.integration.config.TestConfigProvider.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTarantoolClientAwareTest {
    protected TarantoolTemplate tarantoolTemplate;

    public abstract TarantoolClientOptions getOptions();

    @BeforeAll
    void setUp() {
        TarantoolMappingContext mappingContext = mappingContext();
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client = clientFactory(getOptions()).createClient();
        TarantoolRequestRetryPolicies.InfiniteRetryPolicyFactory<Predicate<Throwable>> retryPolicyFactory = new TarantoolRequestRetryPolicies.InfiniteRetryPolicyFactory<>(1, 1, 1, t -> false);
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> retryingClient = new RetryingTarantoolTupleClient(client, retryPolicyFactory);
        tarantoolTemplate = new TarantoolTemplate(retryingClient, converter(mappingContext), exceptionTranslator());
    }

}