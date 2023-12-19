package com.example.socialnetwork.domain;


import java.util.*;

public class User extends Entity<Long>{
    private String firstName;
    private String lastName;
    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public User(Long id, String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
        this.setId(id);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    @Override
    public String toString() {
        return
                "(id:" + this.getId() + ")" +
                        firstName +
                        lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(this.getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
