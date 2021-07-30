package org.springframework.data.tarantool.integration.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.tarantool.core.mapping.Field;
import org.springframework.data.tarantool.core.mapping.Space;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Space("users")
public class User {
	@Id
	private UUID id;

	private String firstName;
	@Field("last_name")
	private String lastName;
	private LocalDate birthDate;
	private Integer age;
	private Boolean active;
	private String email;
	private Address address;

	@Version
	private Long version;
}
