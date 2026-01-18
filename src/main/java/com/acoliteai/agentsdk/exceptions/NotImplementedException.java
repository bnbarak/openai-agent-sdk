package com.acoliteai.agentsdk.exceptions;

/**
 * Exception thrown when a method is not yet implemented.
 *
 * <p>This is used during the initial porting phase from TypeScript to Java.
 */
public class NotImplementedException extends RuntimeException {

  public NotImplementedException(String message) {
    super(message);
  }

  public NotImplementedException(String message, Throwable cause) {
    super(message, cause);
  }
}
