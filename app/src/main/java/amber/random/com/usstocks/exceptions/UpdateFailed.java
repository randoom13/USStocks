package amber.random.com.usstocks.exceptions;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

public class UpdateFailed extends Exception {
    private static final int SINVALID_TOKEN = 403;
    public final String mError_code;
    public final String mError_message;
    public final Throwable mInnerException;


    public UpdateFailed(Throwable innerException) {
        mInnerException = innerException;
        mError_code = null;
        mError_message = null;
    }

    public boolean invalidToken() {
        if (!(mInnerException instanceof HttpException))
            return false;
        HttpException exception = (HttpException) mInnerException;
        return exception.response() != null && exception.response().code() == 403;
    }
}
