package com.udemy.db;

import com.udemy.core.Bookmark;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class BookmarkDAO extends AbstractDAO<Bookmark> {

    public BookmarkDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Bookmark> findByUserId(int id) {
        return list(namedTypedQuery("com.udemy.core.Bookmark.findByUserId")
                .setParameter("id", id));
    }

    public Optional<Bookmark> findById(int id) {
        return Optional.ofNullable(get(id));
    }


    public Optional<Bookmark> findByIdAndUserId(int id, int userId) {
        return Optional.ofNullable(
                uniqueResult(
                        namedTypedQuery("com.udemy.core.Bookmark.findByIdAndUserId")
                                .setParameter("id", id)
                                .setParameter("userId", userId)
                )
        );
    }


    public Bookmark save(Bookmark bookmark) {
        return persist(bookmark);
    }


    public void delete(Integer id) {
        namedQuery("com.udemy.core.Bookmark.remove")
                .setParameter("id", id)
                .executeUpdate();
    }

}
