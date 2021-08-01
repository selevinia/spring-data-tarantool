# Spring Data Tarantool
The primary goal of the [Spring Data](https://projects.spring.io/spring-data)
project is to make it easier to build Spring-powered applications that
use new data access technologies such as non-relational databases,
map-reduce frameworks, and cloud based data services.

The Spring Data Tarantool project aims to provide a familiar and consistent Spring-based programming
model to work with Tarantool spaces from Java Spring project using reactive types, such as Mono and Flux
from [Project Reactor](https://projectreactor.io/) like Spring WebFlux do.

## Features
* Build repositories based on common Spring Data interfaces
* Support for reactive data operations
* JavaConfig support for all Cluster and Single Node Tarantool installations
* Exception Translation to the familiar Spring DataAccessException hierarchy
* Automatic implementation of Repository interfaces including support for custom query methods
* Based on the 0.4.3 Tarantool cartridge-driver for Java

## Tarantool compatibility
|`spring-data-tarantool` Version | Tarantool Version
| :----------- | :----: |
| 0.1.x | 1.10.x, 2.x

## How to use in your project

To add Spring Data Tarantool to a Maven-based project, add the following dependency:
```maven
<dependencies>
	<dependency>
		<groupId>io.github.selevinia</groupId>
		<artifactId>spring-data-tarantool</artifactId>
		<version>${version}</version>
	</dependency>
</dependencies>
```

For Gradle, use the following declaration:
```gradle
dependencies {
    implementation 'io.github.selevinia:spring-data-tarantool'
}
```

## Getting Started
Prepare Tarantool objects:
```lua
    -- Create Articles space
    local articles = box.schema.space.create("articles", { if_not_exists = true })
    articles:format({
        { name = "id", type = "uuid" },
        { name = "bucket_id", type = "unsigned" },
        { name = "article_name", type = "string" },
        { name = "slug", type = "string", is_nullable = true },
        { name = "publish_date", type = "unsigned" },
        { name = "user_id", type = "uuid" },
        { name = "tags", type = "array", is_nullable = true },
        { name = "likes", type = "unsigned", is_nullable = true },
    })

    -- Create required indexes
    articles:create_index("primary", { parts = { { field = "id" } },
                                       if_not_exists = true })
    articles:create_index("bucket_id", { parts = { { field = "bucket_id" } },
                                         unique = false,
                                         if_not_exists = true })
    articles:create_index("article_name", { parts = { { field = "article_name" } },
                                            unique = false,
                                            if_not_exists = true })
    articles:create_index("user_id", { parts = { { field = "user_id" } },
                                       unique = false,
                                       if_not_exists = true })
    
    -- Stored procedure
    local function get_articles_by_user(user)
        return box.space.articles.index.user_id:select(user.id)
    end
```
Use such objects with Spring Data Tarantool Repositories in Java:
```java
public interface ArticleRepository extends TarantoolReactiveRepository<Article, UUID> {

  Flux<Article> findByArticleName(String articleName);

  @Query(function = "get_articles_by_user")
  Flux<Article> findByUser(User user);
}

@Service
public class ArticleService {

    private final ArticleRepository repository;

    @Autowired
    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }
    
    public Mono<Article> doSave(Article article) {
        return repository.save(article);
    }
    
    public Flux<Article> doFind(String articleName) {
        return repository.findByArticleName(articleName);
    }
    
    public Flux<Article> doFindByUser(User user) {
        return repository.findByUser(user);
    }
}

@Configuration
@EnableReactiveTarantoolRepositories
class ApplicationConfig extends AbstractReactiveTarantoolConfiguration {
    
    @Bean
    @Override
    public TarantoolClientOptions tarantoolClientOptions() {
        DefaultTarantoolClientOptions options = new DefaultTarantoolClientOptions();
        options.setNodes(List.of("localhost:3301"));
        options.setUserName("admin");
        options.setPassword("password");
        options.setCluster(true);
        return options;
    }
}
```

See more examples in the module tests.

## License

Spring Data Tarantool is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).