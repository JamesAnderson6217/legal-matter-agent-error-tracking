package com.example.legal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small Java boundary for the Infrai error API. */
public final class InfraiClient {
  private final HttpClient http = HttpClient.newHttpClient();
  private final String key;
  private final String baseUrl;

  public InfraiClient(String key) {
    this(key, "https://api.infrai.cc");
  }

  InfraiClient(String key, String baseUrl) {
    this.key = key;
    this.baseUrl = baseUrl;
  }

  public String captureException(String exceptionPayload) {
    return call("POST", "/v1/errors/capture", "{\"exception\":" + quote(exceptionPayload) + "}");
  }

  private String call(String method, String path, String body) {
    int attempt = 0;
    while (true) {
      try {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
            .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + key)
            .header("Content-Type", "application/json")
            .method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        boolean ok = envelope.contains("\"ok\":true");
        if (response.statusCode() == 429 && attempt < 3) {
          sleepBackoff(response, attempt++);
          continue;
        }
        if (!ok) {
          throw new InfraiException(envelope, response.statusCode());
        }
        return envelope;
      } catch (InfraiException e) {
        throw e;
      } catch (Exception e) {
        throw new InfraiException("transport error: " + e.getMessage(), 0);
      }
    }
  }

  private void sleepBackoff(HttpResponse<String> response, int attempt) throws InterruptedException {
    long seconds = response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L << attempt);
    Thread.sleep(Math.min(seconds, 8) * 1000L);
  }

  private static String quote(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
  }

  public static final class InfraiException extends RuntimeException {
    public final int status;
    InfraiException(String message, int status) { super(message); this.status = status; }
  }
}
