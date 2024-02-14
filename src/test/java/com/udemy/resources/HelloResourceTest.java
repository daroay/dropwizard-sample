package com.udemy.resources;

import com.udemy.auth.HelloAuthenticator;
import com.udemy.auth.HelloAuthorizer;
import com.udemy.core.User;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.security.Principal;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class HelloResourceTest {

    private static final Authenticator<BasicCredentials, User> TEST_AUTHENTICATOR = new Authenticator<BasicCredentials, User>() {
        @Override
        public Optional<User> authenticate(BasicCredentials basicCredentials) throws AuthenticationException {
            return Optional.of(new User());
        }
    };

    private static final ResourceExtension EXT = ResourceExtension
            .builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(TEST_AUTHENTICATOR)
                    .setAuthorizer(new HelloAuthorizer())
                    .buildAuthFilter()))
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
            .addResource(new HelloResource())
            .build();

    @Test
    public void testGetGreeting() {
        String expected = "Hello world!";
        String actual = EXT
                .target("/hello")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
        assertEquals(expected,actual);
    }

    @Test
    public void testSecuredGreeting() {
        String credential = "Basic " + Base64.getEncoder()
                .encodeToString("dummyUser:dummyPass".getBytes());
        String expected = "Hello secured world!";
        String actual = EXT
                .target("/hello/secured")
                .request(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.AUTHORIZATION, credential)
                .get(String.class);
        assertEquals(expected,actual);
    }

}