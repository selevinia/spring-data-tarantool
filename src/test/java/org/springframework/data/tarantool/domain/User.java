package org.springframework.data.tarantool.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.tarantool.core.mapping.Field;
import org.springframework.data.tarantool.core.mapping.Space;

import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(of = "id")
@Space("users")
public class User {
	@Id
	private UUID id;

	private String firstName;
	private String lastName;
	private String email;
}
