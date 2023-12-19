package com.example.socialnetwork.domain.validators;

import com.example.socialnetwork.domain.User;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User entity) throws ValidationException {
        if(entity.getFirstName() == "" || entity.getLastName() == ""){
            throw new ValidationException("Utilizatorul nu poate sa fie NULL!");
        }//TODO: implement method validate
    }
}