package amber.random.com.usstocks.exceptions;

public class UnknownFormat extends Exception {
    public final String mHeaderString;

    public UnknownFormat(String headerString) {
        mHeaderString = headerString;
    }
}
