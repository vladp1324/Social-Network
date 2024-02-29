package com.example.socialnetwork.domain;

public class FriendRequest extends Entity<Long> {

    private User user1, user2;
    private FriendRequestStatus status;

    public FriendRequest(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.status = FriendRequestStatus.PENDING;
    }

    public FriendRequest(User user1, User user2, FriendRequestStatus status) {
        this.user1 = user1;
        this.user2 = user2;
        this.status = status;
    }

    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public FriendRequestStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "user1=" + user1 +
                ", user2=" + user2 +
                ", status=" + status.toString() +
                '}';
    }
}