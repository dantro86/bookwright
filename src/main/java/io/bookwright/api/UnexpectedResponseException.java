package io.bookwright.api;

/** API response does not satisfy the status/body contract expected by the test step. */
public class UnexpectedResponseException extends RuntimeException {

  public UnexpectedResponseException(String message) {
    super(message);
  }
}
