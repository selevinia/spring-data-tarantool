package org.springframework.data.tarantool.integration.repository;

import io.tarantool.driver.api.conditions.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.integration.domain.Address;
import org.springframework.data.tarantool.integration.domain.User;
import org.springframework.data.tarantool.repository.ReactiveTarantoolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Class contains all test methods for Tarantool reactive repository with part tree queries usage
 */
public abstract class AbstractReactiveTarantoolPartTreeQueryTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ReactiveTarantoolOperations operations;

    @BeforeEach
    void prepare() {
        operations.delete(Conditions.any(), User.class).then().as(StepVerifier::create).verifyComplete();

        for (int i = 0; i < 4; i++) {
            User user = newUser("Alexey", "Kuzin", 20 + i, true);
            userRepository.save(user).as(StepVerifier::create)
                    .expectNext(user)
                    .verifyComplete();
        }
        User oneMoreUser = newUser("Vasiliy", "Pupkin", 21, false);
        userRepository.save(oneMoreUser).as(StepVerifier::create)
                .expectNext(oneMoreUser)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByLastName() {
        userRepository.findAllByLastName("Kuzin").as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void shouldNotFindAllByLastName() {
        userRepository.findAllByLastName("Unused").as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByBirthDate() {
        userRepository.findAllByBirthDate(LocalDate.now().minusYears(21)).as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByFirstNameAndLastName() {
        userRepository.findAllByFirstNameAndLastName("Alexey", "Kuzin").as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void shouldFindTop2ByFirstNameAndLastName() {
        userRepository.findTop2ByFirstNameAndLastName("Alexey", "Kuzin").as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldFindOneByFirstNameAndLastName() {
        userRepository.findByFirstNameAndLastName("Vasiliy", "Pupkin").as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldNotFindAllByAge() {
        userRepository.findAllByAge("21").as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable instanceof InvalidDataAccessApiUsageException)
                .verify();
    }

    @Test
    void shouldFindFirst2ByAge() {
        userRepository.findFirstByAge(21).as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByAgeLessThan() {
        userRepository.findAllByAgeLessThan(22).as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByAgeLessThanEqual() {
        userRepository.findAllByAgeLessThanEqual(22).as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByAgeGreaterThan() {
        userRepository.findAllByAgeGreaterThan(22).as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByAgeGreaterThanEqual() {
        userRepository.findAllByAgeGreaterThanEqual(22).as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByAgeAfter() {
        userRepository.findAllByAgeAfter(22).as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByAgeBefore() {
        userRepository.findAllByAgeBefore(22).as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByActiveTrue() {
        userRepository.findAllByActiveTrue().as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void shouldFindAllByActiveFalse() {
        userRepository.findAllByActiveFalse().as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldCountByLastName() {
        userRepository.countByLastName("Kuzin").as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    void shouldExistsByLastName() {
        userRepository.existsByLastName("Kuzin").as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldDeleteByLastName() {
        userRepository.deleteByLastName("Kuzin").as(StepVerifier::create)
                .verifyComplete();

        userRepository.findAllByLastName("Kuzin").as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    protected User newUser(String firstName, String lastName, int age, boolean active) {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName(firstName)
                .lastName(lastName)
                .birthDate(LocalDate.now().minusYears(age))
                .age(age)
                .active(active)
                .email("akuzin@mail.ru")
                .address(Address.builder()
                        .city("Kandalaksha")
                        .street("Lenina 12-2")
                        .postcode("123456")
                        .build())
                .build();
    }

    interface UserRepository extends ReactiveTarantoolRepository<User, UUID> {

        Flux<User> findAllByLastName(String lastName);

        Flux<User> findAllByBirthDate(LocalDate birthDate);

        Flux<User> findAllByFirstNameAndLastName(String firstName, String lastName);

        Flux<User> findTop2ByFirstNameAndLastName(String firstName, String lastName);

        Mono<User> findByFirstNameAndLastName(String firstName, String lastName);

        Flux<User> findAllByAge(String age);

        Flux<User> findFirstByAge(Integer age);

        Flux<User> findAllByAgeLessThan(Integer age);

        Flux<User> findAllByAgeLessThanEqual(Integer age);

        Flux<User> findAllByAgeGreaterThan(Integer age);

        Flux<User> findAllByAgeGreaterThanEqual(Integer age);

        Flux<User> findAllByAgeBetween(Integer start, Integer end);

        Flux<User> findAllByAgeAfter(Integer age);

        Flux<User> findAllByAgeBefore(Integer age);

        Flux<User> findAllByActiveTrue();

        Flux<User> findAllByActiveFalse();

        Mono<Long> countByLastName(String lastName);

        Mono<Boolean> existsByLastName(String lastName);

        Mono<Void> deleteByLastName(String lastName);
    }

}
