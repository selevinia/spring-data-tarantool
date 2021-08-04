package org.springframework.data.tarantool.integration.repository.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.tarantool.integration.domain.*;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@UtilityClass
public class TestData {

    public static User newUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Alexey")
                .lastName("Kuzin")
                .birthDate(LocalDate.now().minusYears(24))
                .age(24)
                .active(true)
                .email("akuzin@mail.ru")
                .address(Address.builder()
                        .city("Kandalaksha")
                        .street("Lenina 12-2")
                        .postcode("123456")
                        .build())
                .build();
    }

    public static User newUser(String firstName, String lastName, int age, boolean active) {
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


    public static User newVersionedUser() {
        User user = newUser();
        user.setVersion(0L);
        return user;
    }

    public static DistributedUser newDistributedUser() {
        return DistributedUser.distributedBuilder()
                .id(UUID.randomUUID())
                .firstName("Alexey")
                .lastName("Kuzin")
                .birthDate(LocalDate.now().minusYears(24))
                .age(24)
                .active(true)
                .email("akuzin@mail.ru")
                .address(Address.builder()
                        .city("Kandalaksha")
                        .street("Lenina 12-2")
                        .postcode("123456")
                        .build())
                .build();
    }

    public static TranslatedArticle newTranslatedArticle() {
        return TranslatedArticle.builder()
                .id(
                        TranslatedArticleKey.builder()
                                .articleId(UUID.randomUUID())
                                .locale(Locale.ENGLISH)
                                .build()
                )
                .name("Selevinia eats tarantool")
                .build();
    }

}
