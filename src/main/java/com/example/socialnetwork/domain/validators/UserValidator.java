package com.example.socialnetwork.domain.validators;

import com.example.socialnetwork.domain.User;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User entity) throws ValidationException {
        if (entity.getFirstName().isBlank() || entity.getLastName().isBlank() ||
                entity.getUsername().isBlank() || entity.getPassword().isBlank()) {
            throw new ValidationException("Do not leave blank fields!");
        }
    }
}