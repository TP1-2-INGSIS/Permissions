package com.ingsis.CNC.user;

/** The external users provider (Auth0) failed. Messages never contain credentials. */
public class UsersProviderException extends RuntimeException {

  public UsersProviderException(String message) {
    super(message);
  }

  public UsersProviderException(String message, Throwable cause) {
    super(message, cause);
  }
}
