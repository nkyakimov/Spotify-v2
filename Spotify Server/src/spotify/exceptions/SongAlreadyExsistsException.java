package spotify.exceptions;

public class SongAlreadyExsistsException extends RuntimeException {
    public SongAlreadyExsistsException(String message) {
        super(message);
    }
}
