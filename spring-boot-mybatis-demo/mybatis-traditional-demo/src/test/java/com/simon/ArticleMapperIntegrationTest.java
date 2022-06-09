package com.simon;

import com.simon.entity.Article;
import com.simon.mapper.ArticleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ArticleMapperIntegrationTest {

    @Autowired
    ArticleMapper articleMapper;

    @Test
    public void whenRecordsInDatabase_shouldReturnArticleWithGivenId() {
        Article article = articleMapper.getArticle(1L);

        assertThat(article).isNotNull();
        assertThat(article.getId()).isEqualTo(1L);
        assertThat(article.getAuthor()).isEqualTo("Baeldung");
        assertThat(article.getTitle()).isEqualTo("Working with MyBatis in Spring");
    }
}