package com.example.socialnetwork.domain.validators;

import com.example.socialnetwork.domain.Friendship;

public class ValidatorFriendship implements Validator<Friendship> {
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if (entity.getUser1().equals(entity.getUser2())) {
            throw new ValidationException("Nu te poti adauga pe tine la prieteni!");
        }
    }
}