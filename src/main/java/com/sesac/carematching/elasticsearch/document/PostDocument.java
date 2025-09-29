package com.sesac.carematching.elasticsearch.document;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Getter
@ToString
@NoArgsConstructor
@Document(indexName = "posts")
public class PostDocument {

    @Id
    private Integer id;

    @Field(type = FieldType.Text, name = "title")
    private String title;

    @Field(type = FieldType.Text, name = "content")
    private String content;

    @Field(type = FieldType.Keyword, name = "user_id")
    private String userId;

    @Field(type = FieldType.Date, name = "created_at")
    private Instant createdAt;

    @Builder
    public PostDocument(
            Integer id,
            String title,
            String content,
            String userId,
            Instant createdAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
    }
}
