package ch.maxant.websocketdemo.mcs;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import io.undertow.util.StatusCodes;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class UserResourceIT {

    @Test
    public void testGetAll() throws Exception {


        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri(getBaseUriForLocalhost());
        builder.setAccept(ContentType.JSON);
        RequestSpecification spec = builder.build();

        given(spec)
                .when()
                .get("/all")
                .then()
                .log().body()
                .statusCode(StatusCodes.OK)
                .body("find { it.id == 1 }.name", is("John Smith"));
    }

    public String getBaseUriForLocalhost() {
        return "http://localhost:" + (8080 + Integer.getInteger("swarm.port.offset", 1));
    }
}
