package com.centennial.eventease_backend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "AUTHORITIES", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"USERNAME", "AUTHORITY"})
})
public class Authority {
    @EmbeddedId
    private AuthorityId id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "USERNAME", insertable = false, updatable = false)
    private User user;

    public Authority() {}

    public Authority(AuthorityId id) {
        this.id = id;
    }

    public AuthorityId getId() {
        return id;
    }

    public void setId(AuthorityId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + id +
                ", user=" + user +
                '}';
    }
}
