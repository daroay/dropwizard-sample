package com.udemy.auth;

import com.udemy.DropBookmarksApplication;
import com.udemy.DropBookmarksConfiguration;
import io.dropwizard.core.Application;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AuthIntegrationTest {

    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");
    private static final String TARGET = "http://localhost:8080";
    private static final String PATH = "/hello/secured";

    private static DropwizardAppExtension<DropBookmarksConfiguration> EXT = new DropwizardAppExtension<>(
            DropBookmarksApplication.class,
            CONFIG_PATH
    );

    private static final HttpAuthenticationFeature FEATURE
            = HttpAuthenticationFeature.basic("javaeeeee", "p@ssw0rd");

    private static Client client;

    @BeforeAll
    public static void setUpClass() {
        Application<DropBookmarksConfiguration> application
                = EXT.getApplication();
        try {
            application.run("db", "migrate", "-i DEV", CONFIG_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (NoSuchMethodError e){
            System.out.println("OK");
        }
    }

    @BeforeEach
    void setUp() {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    void tearDown(){
        client.close();
    }

    @Test
    public void testUnauthorizedPath(){
        Response response = client.target(TARGET).path(PATH).request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAuthorizedPath() {
        String expected = "Hello secured world!";
        client.register(FEATURE);
        String actual = client.target(TARGET).path(PATH).request(MediaType.TEXT_PLAIN).get(String.class);
        assertEquals(expected, actual);
    }


}
