package se.skltp.takcache.exceptions;

public class PersistentCacheException extends Exception{
  public PersistentCacheException(String message) {
    super(message);
  }

  public PersistentCacheException(Exception e) {
    super(e);
  }
}
