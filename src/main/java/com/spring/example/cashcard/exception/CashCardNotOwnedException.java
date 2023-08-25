package com.spring.example.cashcard.exception;

public class CashCardNotOwnedException extends CashCardException {
    public CashCardNotOwnedException(String s) {
        super(s);
    }
}
