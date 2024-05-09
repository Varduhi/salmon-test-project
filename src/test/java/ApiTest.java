import entity.Post;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static io.restassured.RestAssured.*;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTest {
    static final String BASE_URI = "http://jsonplaceholder.typicode.com";
    static final int TOP_FREQUENCY_WORDS_LIMIT = 10;

    @BeforeAll
    public static void setUp() {
        RestAssured.config = new RestAssuredConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    public void createPost(){
        Post post = given()
                .param("title", "MyNewPostTitle")
                .param("body", "MyNewPostBody")
                .param("userId", "1")
                .when()
                .post("/posts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response().as(Post.class);

        assertEquals("MyNewPostTitle", post.getTitle());
    }

    @Test
    public void readPost() {
       Post post =  given()
                .when()
                .get("/posts/1")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().as(Post.class);

        assertNotNull(post);
    }

    @Test
    public void updatePost() {
        Post post = given()
                .param("title", "MyUpdatedTitle")
                .when()
                .put("/posts/1")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().as(Post.class);

        assertEquals(1, post.getId());
    }

    @Test
    public void deletePost() {
        when()
                .delete("/posts/1")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void findTopTenMaxFrequencyWords(){

        String postsBodyStr = given()
                .when()
                .get("/posts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().path("body")
                .toString().replaceAll("\\r\\n|\\n|,", "\\s");

        Stream<String> postsBodyStream = Stream.of(postsBodyStr.toLowerCase().split("\\s")).parallel();
        Map<String, Long> wordFrequencyMap = postsBodyStream
                .collect(Collectors.groupingBy(String::toString,Collectors.counting()));

        Map<String, Long> topFrequencyMap = wordFrequencyMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(TOP_FREQUENCY_WORDS_LIMIT)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        List<String> keyList = new ArrayList<>(topFrequencyMap.keySet());
        for(int i = 0; i < topFrequencyMap.size(); i++) {
            String key = keyList.get(i);
            System.out.println((i+1) + ". " + key + " - " + topFrequencyMap.get(key));
        }
    }
}
