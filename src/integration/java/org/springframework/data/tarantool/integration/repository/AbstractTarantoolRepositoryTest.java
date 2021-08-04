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
