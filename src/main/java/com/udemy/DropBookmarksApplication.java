package com.udemy;

import com.udemy.auth.DBAuthenticator;
import com.udemy.auth.PermissiveAuthorizer;
import com.udemy.core.Bookmark;
import com.udemy.core.User;
import com.udemy.db.BookmarkDAO;
import com.udemy.db.UserDAO;
import com.udemy.resources.BookmarksResource;
import com.udemy.resources.HelloResource;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.migrations.MigrationsBundle;
import org.hibernate.SessionFactory;


public class DropBookmarksApplication extends Application<DropBookmarksConfiguration> {

    private final HibernateBundle<DropBookmarksConfiguration> hibernateBundle
            = new HibernateBundle<>(User.class, Bookmark.class) {
        @Override
        public PooledDataSourceFactory getDataSourceFactory(DropBookmarksConfiguration config) {
            return config.getDataSourceFactory();
        }
    };

    public static void main(final String[] args) throws Exception {
        new DropBookmarksApplication().run(args);
    }

    @Override
    public String getName() {
        return "DropBookmarks";
    }


    @Override
    public void initialize(final Bootstrap<DropBookmarksConfiguration> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new MigrationsBundle<>() {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(DropBookmarksConfiguration config) {
                return config.getDataSourceFactory();
            }
        });
    }


    @Override
    public void run(final DropBookmarksConfiguration config,
                    final Environment environment) {
        final UserDAO userDAO = new UserDAO(hibernateBundle.getSessionFactory());
        final BookmarkDAO bookmarkDAO
                = new BookmarkDAO(hibernateBundle.getSessionFactory());
        final DBAuthenticator authenticator
                = new UnitOfWorkAwareProxyFactory(hibernateBundle)
                .create(DBAuthenticator.class,
                        new Class<?>[]{UserDAO.class, SessionFactory.class},
                        new Object[]{userDAO,
                                hibernateBundle.getSessionFactory()});
        environment.jersey().register( new HelloResource());

        environment.jersey().register(
                new AuthValueFactoryProvider.Binder<>(User.class));
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(authenticator)
                        .setAuthorizer(new PermissiveAuthorizer())
                        .setRealm("SECURITY REALM")
                        .buildAuthFilter()));
        environment.jersey().register(new BookmarksResource(bookmarkDAO));
    }

}
