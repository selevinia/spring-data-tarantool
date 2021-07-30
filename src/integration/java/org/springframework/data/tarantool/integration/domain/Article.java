package org.springframework.data.tarantool.integration.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.tarantool.core.mapping.Field;
import org.springframework.data.tarantool.core.mapping.Space;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(of = "id")
@Space("articles")
public class Article {
    @Id
    private UUID id;

    @Field("article_name")
    private String name;
    private String slug;
    @Field("publish_date")
    private LocalDateTime publishDate;
    @Field("user_id")
    private UUID userId;

    private List<Tag> tags;
    private Integer likes;
}
