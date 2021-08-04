package org.springframework.data.tarantool.integration.repository.query;

import io.tarantool.driver.api.conditions.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.integration.domain.User;
import org.springframework.data.tarantool.repository.TarantoolRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newUser;

/**
 * Class contains all test methods for Tarantool repository with part tree queries usage
 */
public abstract class AbstractTarantoolPartTreeQueryTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TarantoolOperations operations;

    @BeforeEach
    void prepare() {
        operations.delete(Conditions.any(), User.class);

        for (int i = 0; i < 4; i++) {
            User user = newUser("Alexey", "Kuzin", 20 + i, true);
            userRepository.save(user);
        }
        User oneMoreUser = newUser("Vasiliy", "Pupkin", 21, false);
        userRepository.save(oneMoreUser);
    }

    @Test
    void shouldFindAllByLastName() {
        List<User> found = userRepository.findAllByLastName("Kuzin");
        assertThat(found).hasSize(4);
    }

    @Test
    void shouldNotFindAllByLastName() {
        List<User> notFound = userRepository.findAllByLastName("Unused");
        assertThat(notFound).isEmpty();
    }

    protected interface UserRepository extends TarantoolRepository<User, UUID> {

        List<User> findAllByLastName(String lastName);

        List<User> findAllByBirthDate(LocalDate birthDate);

        List<User> findAllByFirstNameAndLastName(String firstName, String lastName);

        List<User> findTop2ByFirstNameAndLastName(String firstName, String lastName);

        User findByFirstNameAndLastName(String firstName, String lastName);

        List<User> findAllByAge(String age);

        List<User> findFirstByAge(Integer age);

        List<User> findAllByAgeLessThan(Integer age);

        List<User> findAllByAgeLessThanEqual(Integer age);

        List<User> findAllByAgeGreaterThan(Integer age);

        List<User> findAllByAgeGreaterThanEqual(Integer age);

        List<User> findAllByAgeBetween(Integer start, Integer end);

        List<User> findAllByAgeAfter(Integer age);

        List<User> findAllByAgeBefore(Integer age);

        List<User> findAllByActiveTrue();

        List<User> findAllByActiveFalse();

        Long countByLastName(String lastName);

        Boolean existsByLastName(String lastName);

        void deleteByLastName(String lastName);
    }
}
