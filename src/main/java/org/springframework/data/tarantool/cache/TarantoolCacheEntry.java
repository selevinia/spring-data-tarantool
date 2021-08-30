package org.springframework.data.tarantool.cache;

import org.springframework.data.tarantool.core.mapping.Field;
import org.springframework.data.tarantool.core.mapping.PrimaryKey;

import java.time.LocalDateTime;

/**
 * {@link TarantoolCacheEntry} used for work with Tarantool space
 *
 * @author Tatiana Blinova
 */
public class TarantoolCacheEntry {
    @PrimaryKey
    private byte[] key;
    private byte[] value;
    @Field("expiry_time")
    private LocalDateTime expiryTime;

    public TarantoolCacheEntry(byte[] key, byte[] value, LocalDateTime expiryTime) {
        this.key = key;
        this.value = value;
        this.expiryTime = expiryTime;
    }

    public static TarantoolCacheEntry of(byte[] key, byte[] value, LocalDateTime expiryTime) {
        return new TarantoolCacheEntry(key, value, expiryTime);
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}
