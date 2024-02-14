package com.udemy.resources;

import com.udemy.auth.PermissiveAuthorizer;
import com.udemy.core.Bookmark;
import com.udemy.core.User;
import com.udemy.db.BookmarkDAO;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class BookmarksResourceTest {


    private static final int USER_ID = 1;
    private static final String USERNAME = "Coda";
    private static final String PASSWORD = "Hale";
    private static final int BOOKMARK_ID = 1;
    private static final String URL
            = "https://github.com/javaeeeee/DropBookmarks";
    private static final User USER = new User(USERNAME, PASSWORD);
    private static final BookmarkDAO BOOKMARK_DAO = mock(BookmarkDAO.class);
    private static final HttpAuthenticationFeature FEATURE
            = HttpAuthenticationFeature.basic(USERNAME, PASSWORD);

    private static final BasicCredentialAuthFilter<User> FILTER
            = new BasicCredentialAuthFilter.Builder<User>()
            .setAuthenticator(credentials -> Optional.of(USER))
            .setAuthorizer(new PermissiveAuthorizer())
            .setRealm("SECURITY REALM")
            .buildAuthFilter();


    public static final ResourceExtension RULE
            = ResourceExtension
            .builder()
            .addProvider(new AuthDynamicFeature(FILTER))
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addResource(new BookmarksResource(BOOKMARK_DAO))
            .build();

    private Bookmark expectedBookmark;
    private List<Bookmark> bookmarks;

    @BeforeAll
    public static void beforeClass() {
        USER.setId(USER_ID);
        // Enable automatic authentication.
        RULE.getJerseyTest().client().register(FEATURE);
    }

    /**
     * Initialization before each method.
     */
    @BeforeEach
    public void setUp() {
        bookmarks = new ArrayList<>();
        expectedBookmark = new Bookmark(
                "https://bitbucket.org/dnoranovich/dropbookmarks",
                "Old project version");
        expectedBookmark.setId(2);
        bookmarks.add(expectedBookmark);

        expectedBookmark = new Bookmark(URL, "The repository of this project");
        expectedBookmark.setId(BOOKMARK_ID);
        bookmarks.add(expectedBookmark);
    }

    /**
     * Clean up after each method.
     */
    @AfterEach
    public void dearDown() {
        reset(BOOKMARK_DAO);
        bookmarks.clear();
    }

    /**
     * Test of getBookmarks method, of class BookmarksResource.
     */
    @Test
    public void testGetBookmarks() {
        // given
        when(BOOKMARK_DAO.findByUserId(USER_ID))
                .thenReturn(Collections.unmodifiableList(bookmarks));

        // when
        final List<Bookmark> response = RULE
                .getJerseyTest()
                .target("/bookmarks")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Bookmark>>() {
                });

        //then
        verify(BOOKMARK_DAO).findByUserId(USER_ID);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(bookmarks.size(), response.size());
        assertTrue(response.containsAll(bookmarks));
    }

    /**
     * Test of getBookmark method, of class BookmarksResource.
     */
    @Test
    public void testGetBookmarkFound() {
        // given
        when(BOOKMARK_DAO.findByIdAndUserId(BOOKMARK_ID, USER_ID))
                .thenReturn(Optional.of(expectedBookmark));

        // when
        final Optional<Bookmark> response
                = RULE
                .getJerseyTest()
                .target("/bookmarks/" + BOOKMARK_ID)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<Optional<Bookmark>>() {
                });

        // then
        verify(BOOKMARK_DAO).findByIdAndUserId(BOOKMARK_ID, USER_ID);
        assertNotNull(response);
        assertTrue(response.isPresent());
        assertEquals(expectedBookmark, response.get());
    }

    /**
     * Test of getBookmark method, of class BookmarksResource.
     */
    @Test
    public void testGetBookmarkNotFound() {
        // given
        when(BOOKMARK_DAO.findByIdAndUserId(BOOKMARK_ID, USER_ID))
                .thenReturn(Optional.empty());

        // when
        final Response response
                = RULE
                .target("/bookmarks/" + BOOKMARK_ID)
                .request(MediaType.APPLICATION_JSON)
                .get();

        // then
        verify(BOOKMARK_DAO).findByIdAndUserId(BOOKMARK_ID, USER_ID);
        assertNotNull(response);
        assertNull(response.readEntity(Bookmark.class));
        assertEquals(404, response.getStatus());
    }

    /**
     * Test of addBookmark method, of class BookmarksResource.
     */
    @Test
    public void testAddBookmarkOK() {
        ArgumentCaptor<Bookmark> argumentCaptor
                = ArgumentCaptor.forClass(Bookmark.class);

        // given
        when(BOOKMARK_DAO.save(any(Bookmark.class
        )))
                .thenReturn(expectedBookmark);

        // when
        final Bookmark response
                = RULE
                .getJerseyTest()
                .target("/bookmarks")
                .request(MediaType.APPLICATION_JSON)
                .post(
                        Entity.entity(
                                expectedBookmark,
                                MediaType.APPLICATION_JSON),
                        Bookmark.class);
        // then
        assertNotNull(response);
        verify(BOOKMARK_DAO)
                .save(argumentCaptor.capture());

        Bookmark value = argumentCaptor.getValue();
        assertNotNull(value);
        assertNotNull(value.getUser());
        assertEquals(value.getUser(), USER);

        assertEquals(expectedBookmark, response);
    }

    /**
     * Test of addBookmark method, of class BookmarksResource.
     */
    @Test
    public void testAddBookmarkInvalid() {
        final Response response
                = RULE
                .getJerseyTest()
                .target("/bookmarks")
                .request(MediaType.APPLICATION_JSON)
                .post(
                        Entity.json(
                                new Bookmark(null, null)));

        assertEquals(422, response.getStatus());
    }

    /**
     * Test of modifyBookmark method, of class BookmarksResource.
     */
    @Test
    public void testModifyBookmarkOK() {
        String expectedURL
                = "https://github.com/javaeeeee/SpringBootBookmarks";
        ArgumentCaptor<Bookmark> argumentCaptor
                = ArgumentCaptor.forClass(Bookmark.class);

        Bookmark bookmarkWithModifications
                = new Bookmark(expectedURL, null);
        bookmarkWithModifications.setId(109678);

        // given
        when(BOOKMARK_DAO.findByIdAndUserId(BOOKMARK_ID, USER_ID))
                .thenReturn(Optional.of(expectedBookmark));
        when(BOOKMARK_DAO.save(any(Bookmark.class)))
                .thenReturn(expectedBookmark);

        // when
        Bookmark response = RULE
                .getJerseyTest()
                .target("/bookmarks/" + BOOKMARK_ID)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(
                                bookmarkWithModifications,
                                MediaType.APPLICATION_JSON),
                        Bookmark.class);

        // then
        assertNotNull(response);
        assertEquals(expectedURL, response.getUrl());
        assertEquals(expectedBookmark.getDescription(),
                response.getDescription());
        assertEquals(expectedBookmark.getUser(),
                response.getUser());

        verify(BOOKMARK_DAO).save(argumentCaptor.capture());
        assertNotNull(argumentCaptor.getValue());
        assertEquals(expectedURL, argumentCaptor.getValue().getUrl());
        assertNotEquals(URL, argumentCaptor.getValue().getUrl());
        assertEquals(expectedBookmark.getDescription(),
                argumentCaptor.getValue().getDescription());
        assertEquals(expectedBookmark.getUser(),
                argumentCaptor.getValue().getUser());
        // Check that purgeMap was called.
        assertEquals(BOOKMARK_ID,
                argumentCaptor.getValue().getId().intValue());
    }

    /**
     * Test of modifyBookmark method, of class BookmarksResource.
     */
    @Test
    public void testModifyBookmarkNotFound() {
        // given
        when(BOOKMARK_DAO.findByIdAndUserId(BOOKMARK_ID, USER_ID))
                .thenReturn(Optional.empty());

        // when
        Response response = RULE
                .target("/bookmarks/" + BOOKMARK_ID)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(
                                expectedBookmark,
                                MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());

        // then
    }

    /**
     * Test of modifyBookmark method, of class BookmarksResource.
     */
    @Test
    public void testModifyBookmarkInvalid() {
        String expectedKey = "wrongKey";

        // given
        when(BOOKMARK_DAO.findByIdAndUserId(BOOKMARK_ID, USER_ID))
                .thenReturn(Optional.of(expectedBookmark));

        // when
        Response response = RULE
                .getJerseyTest()
                .target("/bookmarks/" + BOOKMARK_ID)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(expectedKey,
                        MediaType.APPLICATION_JSON));

        // then
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());

        verify(BOOKMARK_DAO, times(0)).save(any(Bookmark.class));
    }

    /**
     * Test of modifyBookmark method, of class BookmarksResource.
     */
    @Test
    public void testModifyBookmarkInvalidException() {
        String expectedKey = "wrongKey";

        // given
        when(BOOKMARK_DAO.findByIdAndUserId(BOOKMARK_ID, USER_ID))
                .thenReturn(Optional.of(expectedBookmark));

        // when
        Response response = RULE
                .getJerseyTest()
                .target("/bookmarks/" + BOOKMARK_ID)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(expectedKey,
                        MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    /**
     * Test of deleteBookmark method, of class BookmarksResource.
     */
    @Test
    public void testDeleteBookmarkOK() {
        // given
        when(BOOKMARK_DAO.findByIdAndUserId(BOOKMARK_ID, USER_ID))
                .thenReturn(Optional.of(expectedBookmark));

        //when
        Bookmark response = RULE
                .getJerseyTest()
                .target("/bookmarks/" + BOOKMARK_ID)
                .request(MediaType.APPLICATION_JSON)
                .delete(Bookmark.class);

        //then
        assertNotNull(response);
        assertEquals(expectedBookmark, response);

        verify(BOOKMARK_DAO).delete(BOOKMARK_ID);
    }

    /**
     * Test of deleteBookmark method, of class BookmarksResource.
     */
    @Test
    public void testDeleteBookmarkNotFound() {
        // given
        when(BOOKMARK_DAO.findByIdAndUserId(BOOKMARK_ID, USER_ID))
                .thenReturn(Optional.empty());

        // when
        Response response = RULE
                .getJerseyTest()
                .target("/bookmarks/" + BOOKMARK_ID)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        // then
        assertEquals(404, response.getStatus());

    }

    /**
     * Test of purgeMap() method
     */
    @Test
    public void testPurgeMap() {
        String expectedKey = "url";
        BookmarksResource sut = new BookmarksResource(BOOKMARK_DAO);
        Map<String, String> map = new HashMap<>();
        map.put("id", "1");
        map.put(expectedKey, "http://www.dropwizard.io/1.0.2/docs/");
        map.put("description", null);

        sut.purgeMap(map);

        assertNotNull(map);
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        assertTrue(map.containsKey(expectedKey));
    }
}