package com.udemy.db;

import com.udemy.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractDAO<User> {
    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<User> findAll() {
        return list(
                namedTypedQuery("com.udemy.core.User.findAll")
        );
    }

    public Optional<User> findByUsernameAndPassword(String username, String password){
        return Optional.ofNullable(uniqueResult(
                namedTypedQuery("com.udemy.core.User.findByUsernameAndPassword")
                        .setParameter("username", username)
                        .setParameter("password", password)
        ));
    }

    public Optional<User> findByUsername(String username){
        return Optional.ofNullable(uniqueResult(
                namedTypedQuery("com.udemy.core.User.findByUsername")
                        .setParameter("username", username)
        ));
    }

    public Optional<User> findById(Integer id){
        return Optional.ofNullable(uniqueResult(
                namedTypedQuery("com.udemy.core.User.findById")
                        .setParameter("id", id)
        ));
    }
}
