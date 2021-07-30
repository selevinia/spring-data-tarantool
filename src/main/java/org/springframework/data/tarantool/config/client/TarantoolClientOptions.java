package org.springframework.data.tarantool.config.client;

import java.util.List;

/**
 * Basic Tarantool connection options interface
 *
 * @author Alexander Rublev
 * @see TarantoolClientFactory
 */
public interface TarantoolClientOptions {

    /**
     * List of Tarantool nodes (host:port) to connect to
     *
     * @return Tarantool nodes
     */
    List<String> getNodes();

    /**
     * Tarantool user name
     *
     * @return user name
     */
    String getUserName();

    /**
     * Tarantool user password
     *
     * @return user password
     */
    String getPassword();

    /**
     * The number of connections used for sending requests to the server
     *
     * @return number of connections
     */
    int getConnections();

    /**
     * The timeout for connecting to the Tarantool server, in milliseconds
     *
     * @return connect timeout
     */
    int getConnectTimeout();

    /**
     * The timeout for reading the responses from Tarantool server, in milliseconds
     *
     * @return read timeout
     */
    int getReadTimeout();

    /**
     * The timeout for receiving a response from the Tarantool server, in milliseconds
     *
     * @return request timeout
     */
    int getRequestTimeout();

    /**
     * Tarantool cluster flag
     *
     * @return true if client should connect to Tarantool Cluster
     */
    boolean isCluster();

    /**
     * Tarantool CRUD module installed flag
     *
     * @return true if CRUD module installed
     */
    boolean isCrudAvailable();

}
