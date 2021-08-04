package org.springframework.data.tarantool.integration.repository.query;

import io.tarantool.driver.api.conditions.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.integration.domain.User;
import org.springframework.data.tarantool.repository.TarantoolRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void shouldFindAllByBirthDate() {
        List<User> found = userRepository.findAllByBirthDate(LocalDate.now().minusYears(21));
        assertThat(found).hasSize(2);
    }

    @Test
    void shouldFindAllByFirstNameAndLastName() {
        List<User> found = userRepository.findAllByFirstNameAndLastName("Alexey", "Kuzin");
        assertThat(found).hasSize(4);
    }

    @Test
    void shouldFindTop2ByFirstNameAndLastName() {
        List<User> found = userRepository.findTop2ByFirstNameAndLastName("Alexey", "Kuzin");
        assertThat(found).hasSize(2);
    }

    @Test
    void shouldFindOneByFirstNameAndLastName() {
        User found = userRepository.findByFirstNameAndLastName("Vasiliy", "Pupkin");
        assertThat(found).isNotNull();
    }

    @Test
    void shouldNotFindAllByAge() {
        assertThatThrownBy(() -> userRepository.findAllByAge("21"))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    @Test
    void shouldFindFirst2ByAge() {
        List<User> found = userRepository.findFirstByAge(21);
        assertThat(found).hasSize(1);
    }

    @Test
    void shouldFindAllByAgeLessThan() {
        List<User> found = userRepository.findAllByAgeLessThan(22);
        assertThat(found).hasSize(3);
    }

    @Test
    void shouldFindAllByAgeLessThanEqual() {
        List<User> found = userRepository.findAllByAgeLessThanEqual(22);
        assertThat(found).hasSize(4);
    }

    @Test
    void shouldFindAllByAgeGreaterThan() {
        List<User> found = userRepository.findAllByAgeGreaterThan(22);
        assertThat(found).hasSize(1);
    }

    @Test
    void shouldFindAllByAgeGreaterThanEqual() {
        List<User> found = userRepository.findAllByAgeGreaterThanEqual(22);
        assertThat(found).hasSize(2);
    }

    @Test
    void shouldFindAllByAgeAfter() {
        List<User> found = userRepository.findAllByAgeAfter(22);
        assertThat(found).hasSize(1);
    }

    @Test
    void shouldFindAllByAgeBefore() {
        List<User> found = userRepository.findAllByAgeBefore(22);
        assertThat(found).hasSize(3);
    }

    @Test
    void shouldFindAllByActiveTrue() {
        List<User> found = userRepository.findAllByActiveTrue();
        assertThat(found).hasSize(4);
    }

    @Test
    void shouldFindAllByActiveFalse() {
        List<User> found = userRepository.findAllByActiveFalse();
        assertThat(found).hasSize(1);
    }

    @Test
    void shouldCountByLastName() {
        Long count = userRepository.countByLastName("Kuzin");
        assertThat(count).isEqualTo(4L);
    }

    @Test
    void shouldExistsByLastName() {
        Boolean exists = userRepository.existsByLastName("Kuzin");
        assertThat(exists).isTrue();
    }

    @Test
    void shouldDeleteByLastName() {
        userRepository.deleteByLastName("Kuzin");

        List<User> notFound = userRepository.findAllByLastName("Kuzin");
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
