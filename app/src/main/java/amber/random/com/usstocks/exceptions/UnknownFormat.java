package amber.random.com.usstocks.exceptions;

public class UnknownFormat extends Exception {
    public final String invalidString;

    public UnknownFormat(String message, String invalidString) {
        super(message);
        this.invalidString = invalidString;
    }
}
