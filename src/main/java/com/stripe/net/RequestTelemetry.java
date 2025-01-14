package com.stripe.net;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.stripe.Stripe;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import com.stripe.exception.StripeException;
import com.stripe.util.Stopwatch;
import lombok.Data;

/** Helper class used by {@link LiveStripeResponseGetter} to manage request telemetry. */
class RequestTelemetry {
  /** The name of the header used to send request telemetry in requests. */
  public static final String HEADER_NAME = "X-Stripe-Client-Telemetry";

  private static final int MAX_REQUEST_METRICS_QUEUE_SIZE = 100;

  private static final Gson gson = new Gson();

  private static ConcurrentLinkedQueue<RequestMetrics> prevRequestMetrics =
      new ConcurrentLinkedQueue<RequestMetrics>();

  /**
   * Returns an {@link Optional} containing the value of the {@code X-Stripe-Telemetry} header to
   * add to the request. If the header is already present in the request, or if there is available
   * metrics, or if telemetry is disabled, then the returned {@code Optional} is empty.
   *
   * @param headers the request headers
   */
  public Optional<String> getHeaderValue(HttpHeaders headers) {
    if (headers.firstValue(HEADER_NAME).isPresent()) {
      return Optional.empty();
    }

    RequestMetrics requestMetrics = prevRequestMetrics.poll();
    if (requestMetrics == null) {
      return Optional.empty();
    }

    if (!Stripe.enableTelemetry) {
      return Optional.empty();
    }

    ClientTelemetryPayload payload = new ClientTelemetryPayload(requestMetrics);
    return Optional.of(gson.toJson(payload));
  }

  /**
   * Sends a Stripe request with telemetry data and returns the response.
   *
   * @param <T> The type of the response expected, extending AbstractStripeResponse.
   * @param request The StripeRequest to be sent, potentially with additional telemetry headers.
   * @param send The function used to send the request and receive the response.
   * @return The response received from sending the request, of type T.
   * @throws StripeException If an error potentially occurs in the implementation during request processing or response handling.
   */
  public <T extends AbstractStripeResponse<?>> T sendWithTelemetry(
    StripeRequest request, RequestSendFunction<T> send) throws StripeException {
    Optional<String> telemetryHeaderValue = getHeaderValue(request.headers());
    if (telemetryHeaderValue.isPresent()) {
      request =
        request.withAdditionalHeader(HEADER_NAME, telemetryHeaderValue.get());
    }

    Stopwatch stopwatch = Stopwatch.startNew();

    T response = send.apply(request);

    stopwatch.stop();

    maybeEnqueueMetrics(response, stopwatch.getElapsed());

    return response;
  }

  /**
   * If telemetry is enabled and the queue is not full, then enqueue a new metrics item; otherwise,
   * do nothing.
   *
   * @param response the Stripe response
   * @param duration the request duration
   */
  public void maybeEnqueueMetrics(AbstractStripeResponse<?> response, Duration duration) {
    if (!Stripe.enableTelemetry) {
      return;
    }

    if (response.requestId() == null) {
      return;
    }

    if (prevRequestMetrics.size() >= MAX_REQUEST_METRICS_QUEUE_SIZE) {
      return;
    }

    RequestMetrics metrics = new RequestMetrics(response.requestId(), duration.toMillis());
    prevRequestMetrics.add(metrics);
  }

  @Data
  private static class ClientTelemetryPayload {
    @SerializedName("last_request_metrics")
    private final RequestMetrics lastRequestMetrics;
  }

  @Data
  private static class RequestMetrics {
    @SerializedName("request_id")
    private final String requestId;

    @SerializedName("request_duration_ms")
    private final long requestDurationMs;
  }
}
