
package com.udemy.db;

import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class DAOTest {

    /**
     * Hibernate session factory.
     */
    protected static final SessionFactory SESSION_FACTORY
            = HibernateUtil.getSessionFactory();
    /**
     * A handle to apply Liquibase DB refactorings programmatically.
     */
    protected static Liquibase liquibase = null;

    /**
     * Initializations before all test methods.
     * http://myjourneyonjava.blogspot.ca/2014/12/different-ways-to-get-connection-object.html
     *
     */
    @BeforeAll
    public static void setUpClass() {
        final Session session = SESSION_FACTORY.openSession();
        session.doWork(connection -> {
            final Database database;
            try {
                database = DatabaseFactory
                        .getInstance()
                        .findCorrectDatabaseImplementation(
                                new JdbcConnection(connection)
                        );
                liquibase = new Liquibase(
                        "migrations.xml",
                        new ClassLoaderResourceAccessor(),
                        database
                );
            } catch (LiquibaseException | NoSuchMethodError e) {
                e.printStackTrace();
            }
        });
        session.close();
    }

    /**
     * Clean up after all test methods.
     */
    @AfterAll
    public static void tearDownClass() {
        //SESSION_FACTORY.close();
    }
    /**
     * Hibernate session.
     */
    protected Session session;
    /**
     * Hibernate transaction.
     */
    protected Transaction tx;

    /**
     * Initializations before each test method.
     *
     * @throws LiquibaseException if something is wrong with Liquibase.
     */
    @BeforeEach
    public abstract void setUp() throws LiquibaseException;

    /**
     * Cleanup after each test method.
     *
     * @throws DatabaseException if there is an error with database access.
     * @throws LockException if two clients try to apply migrations
     * simultaneously.
     */
    @AfterEach
    public abstract void tearDown() throws DatabaseException, LockException;

}