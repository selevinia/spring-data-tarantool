package org.springframework.data.tarantool.config.client;

/**
 * Function names in a Tarantool instance or a Tarantool Cartridge role for CRUD operations.
 *
 * @author Alexander Rublev
 */
public interface OperationMappingOptions {

    /**
     * Get API function name for getting the spaces and indexes schema
     *
     * @return a callable API function name
     */
    String getGetSchemaFunctionName();

    /**
     * Get API function name for performing the delete operation
     *
     * @return a callable API function name
     */
    String getDeleteFunctionName();

    /**
     * Get API function name for performing the insert operation
     *
     * @return a callable API function name
     */
    String getInsertFunctionName();

    /**
     * Get API function name for performing the replace operation
     *
     * @return a callable API function name
     */
    String getReplaceFunctionName();

    /**
     * Get API function name for performing the update operation
     *
     * @return a callable API function name
     */
    String getUpdateFunctionName();

    /**
     * Get API function name for performing the upsert operation
     *
     * @return a callable API function name
     */
    String getUpsertFunctionName();

    /**
     * Get API function name for performing the select operation
     *
     * @return a callable API function name
     */
    String getSelectFunctionName();

}
