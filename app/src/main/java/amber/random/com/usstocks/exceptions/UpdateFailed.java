package amber.random.com.usstocks.exceptions;

/**
 * Created by akhlivnyuk on 6/22/2017.
 */

public class UpdateFailed extends Exception {
    public final String mError_code;
    public final String mError_message;
    public final boolean isInternetException;
    public final Exception mInnerException;
    private final String INVALID_TOKEN = "invalid_auth_token";

    public UpdateFailed(String error_code, String error_message) {
        mError_code = error_code;
        mError_message = error_message;
        isInternetException = true;
        mInnerException = null;
    }

    public UpdateFailed(Exception innerException) {
        mInnerException = innerException;
        mError_code = null;
        mError_message = null;
        isInternetException = false;
    }

    public boolean invalidToken() {
        return isInternetException && mError_code.equals(INVALID_TOKEN);
    }
}
