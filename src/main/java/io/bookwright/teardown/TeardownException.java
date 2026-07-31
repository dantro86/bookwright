package io.bookwright.teardown;

/** Raised when cleanup fails after an otherwise successful test. */
public class TeardownException extends RuntimeException {

  public TeardownException(String message) {
    super(message);
  }
}
