package com.mymicroservice.userservice.exception;

import jakarta.persistence.EntityNotFoundException;

public class CardInfoNotFoundException extends EntityNotFoundException {

    public CardInfoNotFoundException(String message) {
        super(message);
    }
}
