package org.springframework.data.tarantool.integration.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.tarantool.core.mapping.Field;
import org.springframework.data.tarantool.core.mapping.Space;

import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(of = "id")
@Space("comments")
public class Comment {
    @Id
    private UUID id;

    @Field("article_id")
    private UUID articleId;
    @Field("user_id")
    private UUID userId;
    private String value;
    private Integer likes;
}
