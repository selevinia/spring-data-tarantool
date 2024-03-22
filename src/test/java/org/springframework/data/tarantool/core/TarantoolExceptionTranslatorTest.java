package org.springframework.data.tarantool.core;

import io.tarantool.driver.api.TarantoolServerAddress;
import io.tarantool.driver.core.metadata.TarantoolSpaceMetadataImpl;
import io.tarantool.driver.exceptions.*;
import org.junit.jupiter.api.Test;
import org.msgpack.value.impl.ImmutableBinaryValueImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.data.tarantool.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TarantoolExceptionTranslatorTest {
    private final TarantoolExceptionTranslator et = new DefaultTarantoolExceptionTranslator();

    @Test
    void shouldTranslateNoAvailableConnectionsException() {
        DataAccessException result = et.translateExceptionIfPossible(new NoAvailableConnectionsException());

        assertThat(result).isInstanceOf(TarantoolServerConnectionException.class)
                .hasMessageStartingWith("No available connections").hasCauseInstanceOf(NoAvailableConnectionsException.class);
    }

    @Test
    void shouldTranslateTarantoolConnectionException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolConnectionException(new RuntimeException("test")));

        assertThat(result).isInstanceOf(TarantoolServerConnectionException.class)
                .hasMessageStartingWith("The client is not connected to Tarantool server").hasCauseInstanceOf(TarantoolConnectionException.class);
    }

    @Test
    void shouldTranslateTarantoolSocketException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolSocketException("test message", new TarantoolServerAddress()));

        assertThat(result).isInstanceOf(TarantoolServerConnectionException.class)
                .hasMessageStartingWith("test message").hasCauseInstanceOf(TarantoolSocketException.class);
    }

    @Test
    void shouldTranslateTarantoolFieldNotFoundException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolFieldNotFoundException("testSpace", new TarantoolSpaceMetadataImpl()));

        assertThat(result).isInstanceOf(TarantoolDataAccessException.class)
                .hasMessageStartingWith("Field 'testSpace' not found in space").hasCauseInstanceOf(TarantoolFieldNotFoundException.class);
    }

    @Test
    void shouldTranslateTarantoolMetadataRequestException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolMetadataRequestException("testFunction", new RuntimeException("test message")));

        assertThat(result).isInstanceOf(TarantoolDataAccessException.class)
                .hasMessageStartingWith("Failed to retrieve space and index metadata using proxy function").hasCauseInstanceOf(TarantoolMetadataRequestException.class);
    }

    @Test
    void shouldTranslateTarantoolSpaceOperationException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolSpaceOperationException("test message"));

        assertThat(result).isInstanceOf(TarantoolDataAccessException.class)
                .hasMessageStartingWith("test message").hasCauseInstanceOf(TarantoolSpaceOperationException.class);
    }

    @Test
    void shouldTranslateTarantoolTupleConversionException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolTupleConversionException(new ImmutableBinaryValueImpl(null), new RuntimeException("test message")));

        assertThat(result).isInstanceOf(TarantoolDataAccessException.class)
                .hasMessageStartingWith("Failed to convert MessagePack value").hasCauseInstanceOf(TarantoolTupleConversionException.class);
    }

    @Test
    void shouldTranslateTarantoolServerException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolInternalException("test message"));

        assertThat(result).isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageStartingWith("test message").hasCauseInstanceOf(TarantoolInternalException.class);
    }

    @Test
    void shouldTranslateTarantoolFunctionCallException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolFunctionCallException("test message"));

        assertThat(result).isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageStartingWith("test message").hasCauseInstanceOf(TarantoolFunctionCallException.class);
    }

    @Test
    void shouldTranslateTarantoolIndexNotFoundException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolIndexNotFoundException(0, "testIndex"));

        assertThat(result).isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageStartingWith("Index 'testIndex' is not found in space").hasCauseInstanceOf(TarantoolIndexNotFoundException.class);
    }

    @Test
    void shouldTranslateTarantoolSpaceFieldNotFoundException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolSpaceFieldNotFoundException("testField"));

        assertThat(result).isInstanceOf(TarantoolSpaceMetadataException.class)
                .hasMessageStartingWith("Field \"testField\" not found in space format metadata").hasCauseInstanceOf(TarantoolSpaceFieldNotFoundException.class);
    }

    @Test
    void shouldTranslateTarantoolSpaceNotFoundException() {
        DataAccessException result = et.translateExceptionIfPossible(new TarantoolSpaceNotFoundException("testSpace"));

        assertThat(result).isInstanceOf(TarantoolSpaceMetadataException.class)
                .hasMessageStartingWith("Space with name 'testSpace' not found").hasCauseInstanceOf(TarantoolSpaceNotFoundException.class);
    }

    @Test
    void shouldTranslateUnknownException() {
        DataAccessException result = et.translateExceptionIfPossible(new RuntimeException("test message"));

        assertThat(result).isInstanceOf(TarantoolUncategorizedException.class)
                .hasMessageStartingWith("test message").hasCauseInstanceOf(RuntimeException.class);
    }
}
