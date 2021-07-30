package org.springframework.data.tarantool.integration.domain;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class DistributedUser extends User {

    private Long bucketId;

    @Builder(builderMethodName = "distributedBuilder")
    public DistributedUser(UUID id, String firstName, String lastName, LocalDate birthDate, int age, boolean active,
                           String email, Address address, Long version, Long bucketId) {
        super(id, firstName, lastName, birthDate, age, active, email, address, version);
        this.bucketId = bucketId;
    }

}
