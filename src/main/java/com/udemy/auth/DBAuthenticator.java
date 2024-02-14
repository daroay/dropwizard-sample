package com.udemy.auth;

import com.udemy.core.User;
import com.udemy.db.UserDAO;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.hibernate.UnitOfWork;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.password.PasswordEncryptor;

public class DBAuthenticator implements Authenticator<BasicCredentials, User> {

    private final UserDAO userDAO;
    private final SessionFactory sessionFactory;
    private final PasswordEncryptor passwordEncryptor
            = new BasicPasswordEncryptor();

    public DBAuthenticator(final UserDAO userDAO,
                           final SessionFactory sessionFactory) {
        this.userDAO = userDAO;
        this.sessionFactory = sessionFactory;
    }

    @UnitOfWork
    @Override
    public final Optional<User> authenticate(BasicCredentials credentials)
            throws AuthenticationException {
        try (Session session = sessionFactory.openSession()) {
            ManagedSessionContext.bind(session);
            return userDAO.findByUsernameAndPassword(credentials.getUsername(), credentials.getPassword());
        } catch (Exception e) {
            throw new AuthenticationException(e);
        } finally {
            ManagedSessionContext.unbind(sessionFactory);
        }

    }

}