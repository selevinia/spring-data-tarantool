package org.springframework.data.tarantool.integration.repository;

import io.tarantool.driver.api.conditions.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.integration.domain.DistributedUser;
import org.springframework.data.tarantool.integration.domain.TranslatedArticle;
import org.springframework.data.tarantool.integration.domain.TranslatedArticleKey;
import org.springframework.data.tarantool.integration.domain.User;
import org.springframework.data.tarantool.repository.Query;
import org.springframework.data.tarantool.repository.TarantoolRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertWith;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newTranslatedArticle;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newUser;

/**
 * Class contains all test methods for Tarantool repository usage
 */
public abstract class AbstractTarantoolRepositoryTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected DistributedUserRepository distributedUserRepository;

    @Autowired
    protected TranslatedArticleRepository translatedArticleRepository;

    @Autowired
    protected TarantoolOperations operations;

    @BeforeEach
    void cleanup() {
        operations.delete(Conditions.any(), User.class);
    }

    @Test
    void shouldNotFindNotExisted() {
        Optional<User> notFound = userRepository.findById(UUID.randomUUID());
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldDoSaveAndFindById() {
        User user = newUser();

        User saved = userRepository.save(user);
        assertThat(saved).isEqualTo(user);

        Optional<User> found = userRepository.findById(user.getId());
        assertWith(found.orElse(null), actual -> {
            assertThat(actual.getId()).isEqualTo(user.getId());
            assertThat(actual.getVersion()).isEqualTo(0L);
        });
    }

    @Test
    void shouldUpdateVersionedEntity() {
        User user = newUser();
        User primary = userRepository.save(user);
        assertThat(primary).isEqualTo(user);

        primary.setFirstName("New FirstName");
        User secondary = userRepository.save(primary);
        assertWith(secondary, actual -> {
            assertThat(actual.getId()).isEqualTo(user.getId());
            assertThat(actual.getVersion()).isEqualTo(1L);
        });
    }

    @Test
    void shouldDoExists() {
        User user = newUser();
        User saved = userRepository.save(user);
        assertThat(saved).isEqualTo(user);

        boolean exists = userRepository.existsById(user.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void shouldDoDelete() {
        User user = newUser();
        User saved = userRepository.save(user);
        assertThat(saved).isEqualTo(user);

        userRepository.delete(saved);

        boolean exists = userRepository.existsById(saved.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void shouldDoDeleteById() {
        User user = newUser();
        User saved = userRepository.save(user);
        assertThat(saved).isEqualTo(user);

        userRepository.deleteById(saved.getId());

        boolean exists = userRepository.existsById(saved.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindAll() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            User saved = userRepository.save(user);
            assertThat(saved).isEqualTo(user);
        }

        Iterable<User> found = userRepository.findAll();
        assertThat(found).hasSize(4);
    }

    @Test
    void shouldNotFindAll() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            User saved = userRepository.save(user);
            assertThat(saved).isEqualTo(user);
        }

        Iterable<User> found = userRepository.findAllById(List.of(UUID.randomUUID()));
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDoCount() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            User saved = userRepository.save(user);
            assertThat(saved).isEqualTo(user);
        }

        long count = userRepository.count();
        assertThat(count).isEqualTo(4L);
    }

    @Test
    void shouldFindByLastNameUsingQuery() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            User saved = userRepository.save(user);
            assertThat(saved).isEqualTo(user);
        }

        List<User> found = userRepository.findUsersByLastName("Kuzin");
        assertThat(found).hasSize(4);
    }

    @Test
    void shouldCountByLastNameUsingQuery() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            User saved = userRepository.save(user);
            assertThat(saved).isEqualTo(user);
        }

        Long count = userRepository.countUsersByLastName("Kuzin");
        assertThat(count).isEqualTo(4L);
    }

    @Test
    void shouldFindByEntityUsingQuery() {
        User user = newUser();
        User saved = userRepository.save(user);
        assertThat(saved).isEqualTo(user);

        List<User> found = userRepository.findOneUser(user);
        assertWith(found.get(0), actual -> {
            assertThat(actual.getId()).isEqualTo(user.getId());
            assertThat(actual.getFirstName()).isEqualTo(user.getFirstName());
            assertThat(actual.getLastName()).isEqualTo(user.getLastName());
            assertThat(actual.getEmail()).isEqualTo(user.getEmail());
        });
    }

    @Test
    void shouldDoSaveAndFindByIdEntityWithCompositePrimaryKey() {
        TranslatedArticle article = newTranslatedArticle();
        translatedArticleRepository.save(article);

        Optional<TranslatedArticle> found = translatedArticleRepository.findById(article.getId());
        assertWith(found.orElse(null), actual -> {
            assertThat(actual.getId()).isEqualTo(article.getId());
            assertThat(actual.getName()).isEqualTo(article.getName());
            assertThat(actual.getText()).isEqualTo(article.getText());
        });
    }

    protected interface UserRepository extends TarantoolRepository<User, UUID> {

        @Query(function = "find_user_by_user")
        List<User> findOneUser(User user);

        @Query(function = "find_users_by_last_name")
        List<User> findUsersByLastName(String lastName);

        @Query(function = "count_users_by_last_name")
        Long countUsersByLastName(String lastName);

        @Query(function = "upload_users")
        Long uploadUsers(List<User> users);
    }

    protected interface DistributedUserRepository extends TarantoolRepository<DistributedUser, UUID> {
    }

    protected interface TranslatedArticleRepository extends TarantoolRepository<TranslatedArticle, TranslatedArticleKey> {
    }

}
