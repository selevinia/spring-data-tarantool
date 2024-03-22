package org.springframework.data.tarantool.config.client;

import io.tarantool.driver.api.*;
import io.tarantool.driver.api.connection.TarantoolConnectionSelectionStrategies;
import io.tarantool.driver.api.proxy.ProxyOperationsMappingConfig;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.auth.SimpleTarantoolCredentials;
import io.tarantool.driver.core.ClusterTarantoolTupleClient;
import io.tarantool.driver.core.ProxyTarantoolTupleClient;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Factory class to produce certain Tarantool Client using creation options
 *
 * @author Alexander Rublev
 */
public class DefaultTarantoolClientFactory implements TarantoolClientFactory {
    private final TarantoolClientOptions options;

    public DefaultTarantoolClientFactory(TarantoolClientOptions options) {
        this.options = options;
    }

    /**
     * Create and return instance of TarantoolClient, based on factory options
     *
     * @return TarantoolClient implementation instance
     */
    @Override
    public TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> createClient() {
        TarantoolClusterAddressProvider addressProvider = new TarantoolClusterAddressProvider() {

            @Override
            public Collection<TarantoolServerAddress> getAddresses() {
                return options.getNodes().stream().map(n -> new TarantoolServerAddress(fromString(n))).collect(Collectors.toList());
            }

            private InetSocketAddress fromString(String s) {
                try {
                    URI uri = new URI("unused://" + s);
                    String host = uri.getHost();
                    int port = uri.getPort();

                    if (uri.getHost() == null || uri.getPort() == -1) {
                        throw new URISyntaxException(uri.toString(), "URI must have host and port parts");
                    }

                    return new InetSocketAddress(host, port);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        TarantoolClientConfig.Builder configBuilder = new TarantoolClientConfig.Builder();
        if (StringUtils.hasText(options.getUserName()) && options.getPassword() != null) {
            configBuilder.withCredentials(new SimpleTarantoolCredentials(options.getUserName(), options.getPassword()));
        }
        if (options.getConnections() > 0) {
            configBuilder.withConnections(options.getConnections());
        }
        if (options.getConnectTimeout() > 0) {
            configBuilder.withConnectTimeout(options.getConnectTimeout());
        }
        if (options.getReadTimeout() > 0) {
            configBuilder.withReadTimeout(options.getReadTimeout());
        }
        if (options.getRequestTimeout() > 0) {
            configBuilder.withRequestTimeout(options.getRequestTimeout());
        }
        if (options.isCluster()) {
            configBuilder.withConnectionSelectionStrategyFactory(TarantoolConnectionSelectionStrategies.ParallelRoundRobinStrategyFactory.INSTANCE);
        } else {
            configBuilder.withConnectionSelectionStrategyFactory(TarantoolConnectionSelectionStrategies.RoundRobinStrategyFactory.INSTANCE);
        }

        ClusterTarantoolTupleClient clusterClient = new ClusterTarantoolTupleClient(configBuilder.build(), addressProvider);
        if (options.isCrudAvailable()) {
            ProxyOperationsMappingConfig.Builder operationsMappingBuilder = ProxyOperationsMappingConfig.builder();
            if (options instanceof OperationMappingOptions) {
                OperationMappingOptions mappingOptions = (OperationMappingOptions) options;
                if (mappingOptions.getGetSchemaFunctionName() != null) {
                    operationsMappingBuilder.withSchemaFunctionName(mappingOptions.getGetSchemaFunctionName());
                }
                if (mappingOptions.getDeleteFunctionName() != null) {
                    operationsMappingBuilder.withDeleteFunctionName(mappingOptions.getDeleteFunctionName());
                }
                if (mappingOptions.getInsertFunctionName() != null) {
                    operationsMappingBuilder.withInsertFunctionName(mappingOptions.getInsertFunctionName());
                }
                if (mappingOptions.getReplaceFunctionName() != null) {
                    operationsMappingBuilder.withReplaceFunctionName(mappingOptions.getReplaceFunctionName());
                }
                if (mappingOptions.getUpdateFunctionName() != null) {
                    operationsMappingBuilder.withUpdateFunctionName(mappingOptions.getUpdateFunctionName());
                }
                if (mappingOptions.getUpsertFunctionName() != null) {
                    operationsMappingBuilder.withUpsertFunctionName(mappingOptions.getUpsertFunctionName());
                }
                if (mappingOptions.getSelectFunctionName() != null) {
                    operationsMappingBuilder.withSelectFunctionName(mappingOptions.getSelectFunctionName());
                }
            }

            return new ProxyTarantoolTupleClient(clusterClient, operationsMappingBuilder.build());
        }
        return clusterClient;
    }
}
