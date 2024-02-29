package com.example.socialnetwork.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Friendship extends Entity<Long> {
    private LocalDateTime friendsFrom;
    private User user1, user2;
    private Long idUser1, idUser2;

    public Long getIdUser1() {
        return idUser1;
    }

    public Long getIdUser2() {
        return idUser2;
    }

    public void setIdUser1(Long idUser1) {
        this.idUser1 = idUser1;
    }

    public void setIdUser2(Long idUser2) {
        this.idUser2 = idUser2;
    }

    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = friendsFrom;
    }

    public Friendship(User user1, User user2, LocalDateTime friendsFrom) {
        this.user1 = user1;
        this.user2 = user2;
        this.idUser1 = user1.getId();
        this.idUser2 = user2.getId();
        this.friendsFrom = friendsFrom;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(friendsFrom, that.friendsFrom) && Objects.equals(user1, that.user1) && Objects.equals(user2, that.user2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), friendsFrom, user1, user2);
    }

    @Override
    public String toString() {
        return
                friendsFrom + " " +
                        user1 + " " +
                        user2;
    }
}
