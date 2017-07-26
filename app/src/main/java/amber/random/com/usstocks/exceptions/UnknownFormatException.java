package amber.random.com.usstocks.exceptions;

public class UnknownFormatException extends Exception {
    public final String invalidString;

    public UnknownFormatException(String message, String invalidString) {
        super(message);
        this.invalidString = invalidString;
    }
}
