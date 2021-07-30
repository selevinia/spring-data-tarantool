package org.springframework.data.tarantool.integration.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class Address {
    private String city;
    private String street;
    private String postcode;
}
