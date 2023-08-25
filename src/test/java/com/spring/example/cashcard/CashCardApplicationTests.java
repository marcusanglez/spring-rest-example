package com.spring.example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;


import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

	public static final String BASE_APP = "/cashcards";
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void testGetSimpleForExistingData(){

		String url = new MyURL("cashcards", "99").toString();
		System.out.println(url);
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity(url, String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		Number amount = documentContext.read("$.amount");
		assertThat(id).isNotNull();
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void testGetNoData(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity(BASE_APP + "/1000", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		assertThat(response.getBody()).isBlank();
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
	void shouldCreateCashCard(){
		CashCard cashCardToCreate = new CashCard(null, 250.00, null);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.postForEntity(BASE_APP, cashCardToCreate, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI location = response.getHeaders().getLocation();
		System.out.println(location);
		assertThat(location).isNotNull();
		assertThat(location.toString().startsWith("/cashcards/"));

		CashCard cashCardToCreate2 = new CashCard(null, 500.0, "marco");
		ResponseEntity<Void> response2 = restTemplate
				.withBasicAuth("marco", "abc123")
				.postForEntity(BASE_APP, cashCardToCreate2, Void.class);

		assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI location2 = response2.getHeaders().getLocation();
		System.out.println(location2);
		assertThat(location2).isNotNull();
		assertThat(location2.toString().startsWith("/cashcards/"));

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity(location2, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void shouldFindAll(){
		CashCard cashCardToCreate = new CashCard(null, 250.00, "marco");
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.postForEntity(BASE_APP, cashCardToCreate, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI location = response.getHeaders().getLocation();

		assertThat(location).isNotNull();
		assertThat(location.toString().startsWith("/cashcards/"));

		CashCard cashCardToCreate2 = new CashCard(null, 500.0, "marco");
		ResponseEntity<Void> response2 = restTemplate
				.withBasicAuth("marco", "abc123")
				.postForEntity(BASE_APP, cashCardToCreate2, Void.class);
		URI location2 = response2.getHeaders().getLocation();
		assertThat(location2).isNotNull();
		assertThat(location2.toString().startsWith("/cashcards/"));

		String urlWithPaging = BASE_APP + "?page=0&size=3";

		System.out.println("\n\n**************" + urlWithPaging);

		ResponseEntity<String> responseAll =  restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity(urlWithPaging
				, String.class);

		DocumentContext documentContext = JsonPath.parse(responseAll.getBody());
		JSONArray page = documentContext.read("$[*]");
		System.out.println(page.toJSONString());
	}

	@Test
	void shouldReturnOkForAllCashCardsWhenListIsRequested() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void shouldReturnAndVerifyForFindAllCashCards(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());


		JSONArray ids =documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 100.0, 150.0);
	}

	@Test
	void shouldReturnPageForFindAllCashCards(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity("/cashcards?page=0&size=3", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		JSONArray jsonArray = documentContext.read("$..id");
		System.out.println(jsonArray.toJSONString());

		//int len = documentContext.read("$.length()");
		//System.out.println(len);
		assertThat(jsonArray.size()).isEqualTo(3);
	}

	@Test
	void shouldReturnASortedPageOfCashCards() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity("/cashcards?page=0&size=5&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		JSONArray amounts = documentContext.read("$..amount");
		System.out.println("amounts: "+ amounts.toJSONString());
		assertThat(amounts).containsExactly(150.0, 123.45, 100.0);


	}


	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$.content");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly( 100.00, 123.45, 150.00);
	}

	@Test
	void shouldNotReturnACashCardWhenUsingBadCredentials(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("BAD-USER", "BAD-PASSWORD")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		 response = restTemplate
				.withBasicAuth("marco", "BAD-PASSWORD")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwner(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("mango-owns-no-cards", "dontcare")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}


	@Test
	void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/102", String.class); // kumar2's data
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
	void shouldUpdateExistingCard(){
		// update card id 101 with 1000
		HttpEntity<CashCard> cashCardHttpEntity = new HttpEntity<>(new CashCard(101L, 1000.0, "marco"));

		ResponseEntity<Void> putResponse = restTemplate
				.withBasicAuth("marco", "abc123")
				.exchange("/cashcards/101", HttpMethod.PUT, cashCardHttpEntity, Void.class);

		assertThat(putResponse.getStatusCode())
				.isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> response = restTemplate
				.withBasicAuth("marco", "abc123")
				.getForEntity("/cashcards/101", String.class);

		assertThat(response.getStatusCode())
				.isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray jsonArray = documentContext.read("$..amount");

		assertThat(jsonArray).containsExactly(1000.0);
	}

	@Test
	void shouldNotUpdateInexistentCard(){
		// update card id 101 with 1000
		HttpEntity<CashCard> cashCardHttpEntity = new HttpEntity<>(new CashCard(1000001L, 1000.0, "marco"));

		ResponseEntity<Void> putResponse = restTemplate
				.withBasicAuth("marco", "abc123")
				.exchange("/cashcards/1000001", HttpMethod.PUT, cashCardHttpEntity, Void.class);

		assertThat(putResponse.getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateNotOwnedCard(){
		// update card id 101 with 1000
		HttpEntity<CashCard> cashCardHttpEntity = new HttpEntity<>(new CashCard(102L, 1000.0, "marco"));

		ResponseEntity<Void> putResponse = restTemplate
				.withBasicAuth("marco", "abc123")
				.exchange("/cashcards/102", HttpMethod.PUT, cashCardHttpEntity, Void.class);

		assertThat(putResponse.getStatusCode())
				.isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldDeleteOwnedCashCard(){
		HttpEntity<CashCard> cashCardHttpEntity = new HttpEntity<>(new CashCard(102L, 123.45, "kumar2"));

		ResponseEntity<String> deleteResponse = restTemplate
				.withBasicAuth("sergio", "53r610rocks")
				.exchange("/cashcards/102", HttpMethod.DELETE, cashCardHttpEntity, String.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void shouldNotDeleteNotOwnedCashCard(){
		HttpEntity<CashCard> cashCardHttpEntity = new HttpEntity<>(new CashCard(102L, 123.45, "marco"));

		ResponseEntity<String> deleteResponse = restTemplate
				.withBasicAuth("marco", "abc123")
				.exchange("/cashcards/102", HttpMethod.DELETE, cashCardHttpEntity, String.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteInexistentCashCard(){
		HttpEntity<CashCard> cashCardHttpEntity = new HttpEntity<>(new CashCard(10002L, 123.45, "marco"));

		ResponseEntity<String> deleteResponse = restTemplate
				.withBasicAuth("marco", "abc123")
				.exchange("/cashcards/10002", HttpMethod.DELETE, cashCardHttpEntity, String.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	private class MyURL {

		private final String fullString;

		public MyURL(String... segments) {
			fullString = "/"  + Arrays.stream(segments).
					collect(Collectors.joining("/"));
		}
		public String toString(){
			return fullString;
		}
	}
}
