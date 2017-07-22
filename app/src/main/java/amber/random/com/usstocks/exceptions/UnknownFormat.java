package amber.random.com.usstocks.exceptions;

public class UnknownFormat extends Exception {
    public final String invalidString;

    public UnknownFormat(String invalidString) {
        this.invalidString = invalidString;
    }
}
