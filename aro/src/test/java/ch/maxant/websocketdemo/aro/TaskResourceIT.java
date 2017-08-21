package ch.maxant.websocketdemo.aro;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import io.undertow.util.StatusCodes;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class TaskResourceIT {

    @Test
    public void testGetTasks() throws Exception {

        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri(getBaseUriForLocalhost());
        builder.setAccept(ContentType.JSON);
        RequestSpecification spec = builder.build();

        given(spec)
                .when()
                .get("/aro/tasks/0")
                .then()
                .log().body()
                .statusCode(StatusCodes.OK)
                .body("size()", is(0));
    }

    public String getBaseUriForLocalhost() {
        return "http://localhost:" + (8080 + Integer.getInteger("swarm.port.offset", 3));
    }
}
