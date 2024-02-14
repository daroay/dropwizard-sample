package com.udemy.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Objects;

@Entity
@Table(name="bookmarks")
@XmlRootElement
@NamedQueries({
        @NamedQuery(
                name = "com.udemy.core.Bookmark.findAll",
                query = "SELECT b FROM Bookmark b"),
        @NamedQuery(
                name = "com.udemy.core.Bookmark.findById",
                query = "SELECT b FROM Bookmark b WHERE b.id = :id"),
        @NamedQuery(
                name = "com.udemy.core.Bookmark.findByUrl",
                query = "SELECT b FROM Bookmark b WHERE b.url = :url"),
        @NamedQuery(
                name = "com.udemy.core.Bookmark.findByDescription",
                query = "SELECT b FROM Bookmark b WHERE b.description = :description"),
        @NamedQuery(
                name = "com.udemy.core.Bookmark.findByUserId",
                query = "SELECT b FROM Bookmark b WHERE b.user.id = :id"),
        @NamedQuery(
                name = "com.udemy.core.Bookmark.remove",
                query = "DELETE FROM Bookmark b where b.id = :id"),
        @NamedQuery(
                name = "com.udemy.core.Bookmark.findByIdAndUserId",
                query = "SELECT b FROM Bookmark b WHERE b.id = :id AND b.user.id = :userId")})
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    /**
     * Bookmark URL.
     */
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "url")
    private String url;
    /**
     * Bookmark description.
     */
    @Size(max = 2048)
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;


    public Bookmark() {
    }

    /**
     * A constructor to create bookmarks using URL and description.
     *
     * @param url bookmark URL.
     * @param description bookmark description.
     */
    public Bookmark(String url, String description) {
        this.url = url;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id,
                this.url,
                this.description,
                this.user);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bookmark other = (Bookmark) obj;
        return Objects.equals(this.user, other.user)
                && Objects.equals(this.url, other.url)
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Bookmark{" + "id=" + id + ", url=" + url
                + ", description=" + description
                + ", user=" + Objects.toString(user) + '}';
    }

}
