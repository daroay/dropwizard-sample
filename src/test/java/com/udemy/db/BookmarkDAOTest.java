package com.udemy.db;


import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import com.udemy.core.Bookmark;
import com.udemy.core.User;
import com.udemy.db.BookmarkDAO;
import com.udemy.db.DAOTest;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Dmitry Noranovich javaeeeee (at) gmail (dot) com
 */
public class BookmarkDAOTest extends DAOTest {

    /**
     * System under test.
     */
    private BookmarkDAO sut;

    /**
     * Initializations before each test method.
     *
     * @throws LiquibaseException if something is wrong with Liquibase.
     */
    @Override
    public void setUp() throws LiquibaseException {
        liquibase.update("TEST");
        session = SESSION_FACTORY.openSession();
        sut = new BookmarkDAO(SESSION_FACTORY);
        tx = null;
    }

    /**
     * Cleanup after each test method.
     *
     * @throws DatabaseException if there is an error with database access.
     * @throws LockException if two clients try to apply migrations
     * simultaneously.
     */
    @Override
    public void tearDown() throws DatabaseException, LockException {
        liquibase.dropAll();
    }

    /**
     * Test of findByUserId method, of class BookmarkDAO.
     */
    @Test
    public void testFindByUserId() {
        List<Bookmark> bookmarks = null;
        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            //Do something here with UserDAO
            bookmarks = sut.findByUserId(1);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }
        assertNotNull(bookmarks);
        assertFalse(bookmarks.isEmpty());
    }

    /**
     * Test of findById method, of class BookmarkDAO.
     */
    @Test
    public void testFindById() {
        String expectedUrl = "https://github.com/javaeeeee/DropBookmarks";
        String expectedDescription = "Repo for this project";
        // An expectedId of a user added by a migration
        int userId = 1;
        // A generated expectedId of a bookmark
        Integer bmId;
        Optional<Bookmark> optional;
        Bookmark bookmark;

        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            //Add a bookmark
            session
                    .createNativeQuery(
                            "insert into bookmarks values(null, :url, :description, :userId)"
                    )
                    .setParameter("url", expectedUrl)
                    .setParameter("description", expectedDescription)
                    .setParameter("userId", userId)
                    .executeUpdate();

            Long result = (Long) session
                    .createNativeQuery(
                            "select id from bookmarks "
                                    + "where url = :url "
                                    + "and description = :description "
                                    + "and user_id = :userId"
                    )
                    .setParameter("url", expectedUrl)
                    .setParameter("description", expectedDescription)
                    .setParameter("userId", userId)
                    .uniqueResult();

            bmId = result.intValue();

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNotNull(bmId);
        //Reopen session
        session = SESSION_FACTORY.openSession();
        tx = null;

        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            //Look for a bookmark
            optional = sut.findById(bmId);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNotNull(optional);
        assertTrue(optional.isPresent());
        bookmark = optional.get();
        assertEquals(expectedUrl, bookmark.getUrl());
    }

    /**
     * Test of save method, of class BookmarkDAO.
     */
    @Test
    public void testSave() {
        String expectedUrl = "https://github.com/javaeeeee/DropBookmarks";
        String actualUrl;
        String expectedDescription = "Repo for this project";
        // An expectedId of a user added by a migration
        int userId = 1;
        Integer bmID;
        Bookmark addedBookmark = new Bookmark(expectedUrl, expectedDescription);
        UserDAO userDAO = new UserDAO(SESSION_FACTORY);

        // Add a bookmark
        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            //obtain a user
            User user = userDAO.findById(userId).get();
            addedBookmark.setUser(user);
            //Save Bookmark
            bmID = Math.toIntExact(sut.save(addedBookmark).getId());

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNotNull(bmID);
        //Reopen session
        session = SESSION_FACTORY.openSession();
        tx = null;

        // Extract the bookmark;
        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            actualUrl = (String) session
                    .createNativeQuery(
                            "select url from bookmarks "
                                    + "where id = :id"
                    )
                    .setParameter("id", bmID)
                    .uniqueResult();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNotNull(actualUrl);
        assertFalse(actualUrl.isEmpty());
        assertEquals(expectedUrl, actualUrl);
    }

    /**
     * Test of delete method, of class BookmarkDAO.
     */
    @Test
    public void testDelete() {
        String expectedUrl = "https://github.com/javaeeeee/DropBookmarks";
        String actualUrl;
        String expectedDescription = "Repo for this project";
        // An expectedId of a user added by a migration
        int userId = 1;
        // A generated expectedId of a bookmark
        Integer bmId;

        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            //Add a bookmark
            session
                    .createNativeQuery(
                            "insert into bookmarks values(null, :url, :description, :userId)"
                    )
                    .setParameter("url", expectedUrl)
                    .setParameter("description", expectedDescription)
                    .setParameter("userId", userId)
                    .executeUpdate();

            Long result = (Long) session
                    .createNativeQuery(
                            "select id from bookmarks "
                                    + "where url = :url "
                                    + "and description = :description "
                                    + "and user_id = :userId"
                    )
                    .setParameter("url", expectedUrl)
                    .setParameter("description", expectedDescription)
                    .setParameter("userId", userId)
                    .uniqueResult();

            bmId = result.intValue();

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNotNull(bmId);
        //Reopen session
        session = SESSION_FACTORY.openSession();
        tx = null;

        //delete a bookmark
        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            //Delete a bookmark
            sut.delete(bmId);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        //Reopen session
        session = SESSION_FACTORY.openSession();
        tx = null;

        //look for a bookmark
        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            actualUrl = (String) session
                    .createNativeQuery(
                            "select url from bookmarks "
                                    + "where id = :id"
                    )
                    .setParameter("id", bmId)
                    .uniqueResult();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNull(actualUrl);
    }

    /**
     * Test findByIdAndUserId() method
     */
    @Test
    public void findByIdAndUserIdOk() {
        Optional<Bookmark> optional;
        final int expectedId = 1;
        final int expectedUserId = 1;
        //look for a bookmark
        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            optional = sut.findByIdAndUserId(expectedId, expectedUserId);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertEquals(expectedId, optional.get().getId());
    }

    /**
     * Test findByIdAndUserId() method
     */
    @Test
    public void findByIdAndUserIdWrongUserId() {
        Optional<Bookmark> optional;
        final int expectedId = 1;
        final int expectedUserId = 2;
        //look for a bookmark
        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            optional = sut.findByIdAndUserId(expectedId, expectedUserId);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNotNull(optional);
        assertFalse(optional.isPresent());
    }

    /**
     * Test findByIdAndUserId() method
     */
    @Test
    public void findByIdAndUserIdWrongId() {
        Optional<Bookmark> optional;
        final int expectedId = 109678;
        final int expectedUserId = 1;
        //look for a bookmark
        try {
            ManagedSessionContext.bind(session);
            tx = session.beginTransaction();

            optional = sut.findByIdAndUserId(expectedId, expectedUserId);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            ManagedSessionContext.unbind(SESSION_FACTORY);
            session.close();
        }

        assertNotNull(optional);
        assertFalse(optional.isPresent());
    }
}