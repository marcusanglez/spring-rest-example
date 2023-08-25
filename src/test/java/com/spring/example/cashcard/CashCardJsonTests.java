package com.spring.example.cashcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CashCardJsonTests {

    @Autowired
    JacksonTester<CashCard> json;

    @Autowired
    JacksonTester<CashCard[]> jsonList;

    private CashCard[] cashCards;


    @BeforeEach
    void setup(){
        cashCards = new CashCard[]
                {
                new CashCard(99L, 123.45, "marco"),
                new CashCard(100L, 100.00, "marco"),
                new CashCard(101L, 150.00, "marco"),
                new CashCard(102L, 200.00, "sergio")
        };
    }

    @Test
    public void cashCardSerializationTest() throws IOException {
        CashCard cashCard = cashCards[0];

        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(123.45);
    }

    @Test
    public void cashCardDeserializationTest() throws IOException {
        String expected = """
                {
                    "id": 99,
                    "amount": 123.45,
                    "owner": "marco"
                }
                """;
        assertThat(json.parse(expected))
                .isEqualTo(new CashCard(99L, 123.45, "marco"));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
        assertThat(json.parseObject(expected).owner()).isEqualTo("marco");
    }

    @Test
    public void cashCardListSerializationTest() throws IOException {

        //InputStream resourceAsStream = classLoader.getResourceAsStream();
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }


}
