package com.stripe.net;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.stripe.exception.*;
import com.stripe.model.StripeObject;
import com.stripe.net.RequestOptions.RequestOptionsBuilder;

public class LiveStripeResponseGetterTest {

  private static final String TEST_URL = "/v1/accounts/james/external_accounts/1";
  private static final String TEST_API_KEY = "sampleKey";
  private HttpClient mockHttpClient;
  private LiveStripeResponseGetter subject;

  @BeforeEach
  public void setup() {
    mockHttpClient = mock(HttpClient.class);
    subject = new LiveStripeResponseGetter(mockHttpClient);
  }

  @Test
  public void testRequestWithJsonSyntaxException() throws StripeException {
    int code = 400;
    String jsonResponse = "{ \"error\": { \"type\": ";
    StripeResponse mockResponse = new StripeResponse(code, HttpHeaders.of(Collections.emptyMap()), jsonResponse);
    RequestOptionsBuilder requestOptionsBuilder = new RequestOptionsBuilder();
    requestOptionsBuilder.setApiKey(TEST_API_KEY);
    RequestOptions requestOptions = requestOptionsBuilder.build();

    when(mockHttpClient.requestWithRetries(Mockito.any(StripeRequest.class))).thenReturn(mockResponse);

    assertThrows(ApiException.class, () -> subject.request(BaseAddress.API, ApiResource.RequestMethod.GET, TEST_URL, new HashMap<>(), StripeObject.class, requestOptions, ApiMode.V1));
  }

  @Test
  public void testRequestWithIdempotencyException() throws StripeException {
    int statusCode = 400;
    String error_type = "idempotency_error";
    String error_message = "The provided idempotency key is already in use for another request.";
    String jsonResponse = String.format("{ \"error\": { \"type\": \"%s\", \"message\": \"%s\" } }", error_type, error_message);
    StripeResponse mockResponse = new StripeResponse(statusCode, HttpHeaders.of(Collections.emptyMap()), jsonResponse);
    RequestOptionsBuilder requestOptionsBuilder = new RequestOptionsBuilder();
    requestOptionsBuilder.setApiKey(TEST_API_KEY);
    RequestOptions requestOptions = requestOptionsBuilder.build();

    when(mockHttpClient.requestWithRetries(Mockito.any(StripeRequest.class))).thenReturn(mockResponse);

    assertThrows(IdempotencyException.class, () -> subject.request(BaseAddress.API, ApiResource.RequestMethod.GET, TEST_URL, new HashMap<>(), StripeObject.class, requestOptions, ApiMode.V1));
  }

  @Test
  public void testRequestWithAuthenticationException() throws StripeException {
    int statusCode = 401;
    String error_type = "authentication_error";
    String error_message = "Your API key has expired. Please regenerate or obtain a new API key";
    String error_code = "api_key_expired";
    String requestId = "req_123";
    String jsonResponse = String.format("{ \"error\": { \"type\": \"%s\", \"message\": \"%s\", \"code\": \"%s\" } }", error_type, error_message, error_code);
    StripeResponse mockResponse = new StripeResponse(statusCode, HttpHeaders.of(Collections.singletonMap("Request-Id", Collections.singletonList(requestId))), jsonResponse);
    RequestOptionsBuilder requestOptionsBuilder = new RequestOptionsBuilder();
    requestOptionsBuilder.setApiKey(TEST_API_KEY);
    RequestOptions requestOptions = requestOptionsBuilder.build();

    when(mockHttpClient.requestWithRetries(Mockito.any(StripeRequest.class))).thenReturn(mockResponse);

    assertThrows(AuthenticationException.class, () -> subject.request(BaseAddress.API, ApiResource.RequestMethod.GET, TEST_URL, new HashMap<>(), StripeObject.class, requestOptions, ApiMode.V1));
  }

  @Test
  public void testRequestWithCardException() throws StripeException {
    int statusCode = 402;
    String error_type = "card_error";
    String error_message = "There are insufficient funds in the account associated with the card. Please use a different payment method or check with your bank.";
    String error_code = "insufficient_funds";
    String requestId = "req_123";
    String jsonResponse = String.format("{ \"error\": { \"type\": \"%s\", \"message\": \"%s\", \"code\": \"%s\" } }", error_type, error_message, error_code);
    StripeResponse mockResponse = new StripeResponse(statusCode, HttpHeaders.of(Collections.singletonMap("Request-Id", Collections.singletonList(requestId))), jsonResponse);
    RequestOptionsBuilder requestOptionsBuilder = new RequestOptionsBuilder();
    requestOptionsBuilder.setApiKey(TEST_API_KEY);
    RequestOptions requestOptions = requestOptionsBuilder.build();

    when(mockHttpClient.requestWithRetries(Mockito.any(StripeRequest.class))).thenReturn(mockResponse);

    assertThrows(CardException.class, () -> subject.request(BaseAddress.API, ApiResource.RequestMethod.GET, TEST_URL, new HashMap<>(), StripeObject.class, requestOptions, ApiMode.V1));
  }

  @Test
  public void testRequestWithPermissionException() throws StripeException {
    int statusCode = 403;
    String error_type = "permission_error";
    String error_message = "Operation not permitted for standard accounts. Please upgrade your account or check your permissions.";
    String error_code = "not_allowed_on_standard_account";
    String requestId = "req_123";
    String jsonResponse = String.format("{ \"error\": { \"type\": \"%s\", \"message\": \"%s\", \"code\": \"%s\" } }", error_type, error_message, error_code);
    StripeResponse mockResponse = new StripeResponse(statusCode, HttpHeaders.of(Collections.singletonMap("Request-Id", Collections.singletonList(requestId))), jsonResponse);
    RequestOptionsBuilder requestOptionsBuilder = new RequestOptionsBuilder();
    requestOptionsBuilder.setApiKey(TEST_API_KEY);
    RequestOptions requestOptions = requestOptionsBuilder.build();

    when(mockHttpClient.requestWithRetries(Mockito.any(StripeRequest.class))).thenReturn(mockResponse);

    assertThrows(PermissionException.class, () -> subject.request(BaseAddress.API, ApiResource.RequestMethod.GET, TEST_URL, new HashMap<>(), StripeObject.class, requestOptions, ApiMode.V1));
  }

  @Test
  public void testRequestWithRateLimitException() throws StripeException {
    int statusCode = 429;
    String error_type = "rate_limit_error";
    String error_message = "You have exceeded the maximum number of requests per minute. Please wait for a few minutes before trying again.";
    String error_code = "rate_limit";
    String requestId = "req_123";
    String jsonResponse = String.format("{ \"error\": { \"type\": \"%s\", \"message\": \"%s\", \"code\": \"%s\" } }", error_type, error_message, error_code);
    StripeResponse mockResponse = new StripeResponse(statusCode, HttpHeaders.of(Collections.singletonMap("Request-Id", Collections.singletonList(requestId))), jsonResponse);
    RequestOptionsBuilder requestOptionsBuilder = new RequestOptionsBuilder();
    requestOptionsBuilder.setApiKey(TEST_API_KEY);
    RequestOptions requestOptions = requestOptionsBuilder.build();

    when(mockHttpClient.requestWithRetries(Mockito.any(StripeRequest.class))).thenReturn(mockResponse);

    assertThrows(RateLimitException.class, () -> subject.request(BaseAddress.API, ApiResource.RequestMethod.GET, TEST_URL, new HashMap<>(), StripeObject.class, requestOptions, ApiMode.V1));
  }

  @Test
  public void testRequestWithApiException() throws StripeException {
    int statusCode = 500;
    String error_type = "api_error";
    String error_message = "An unexpected error occurred on the server. Please try again later.";
    String error_code = "processing_error";
    String requestId = "req_123";
    String jsonResponse = String.format("{ \"error\": { \"type\": \"%s\", \"message\": \"%s\", \"code\": \"%s\" } }", error_type, error_message, error_code);
    StripeResponse mockResponse = new StripeResponse(statusCode, HttpHeaders.of(Collections.singletonMap("Request-Id", Collections.singletonList(requestId))), jsonResponse);
    RequestOptionsBuilder requestOptionsBuilder = new RequestOptionsBuilder();
    requestOptionsBuilder.setApiKey(TEST_API_KEY);
    RequestOptions requestOptions = requestOptionsBuilder.build();

    when(mockHttpClient.requestWithRetries(Mockito.any(StripeRequest.class))).thenReturn(mockResponse);

    assertThrows(ApiException.class, () -> subject.request(BaseAddress.API, ApiResource.RequestMethod.GET, TEST_URL, new HashMap<>(), StripeObject.class, requestOptions, ApiMode.V1));
  }
}
