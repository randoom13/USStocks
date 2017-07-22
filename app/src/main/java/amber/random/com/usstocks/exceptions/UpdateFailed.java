package amber.random.com.usstocks.exceptions;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

public class UpdateFailed extends Exception {
    private static final int sInvalidToken = 403;
    public final Throwable innerException;

    private UpdateFailed(Throwable exception) {
        innerException = exception;
    }

    public UpdateFailed(UnknownFormat innerException) {
        this((Throwable) innerException);
    }

    public UpdateFailed(HttpException innerException) {
        this((Throwable) innerException);
    }

    public UpdateFailed(android.database.SQLException innerException) {
        this((Throwable) innerException);
    }


    public UpdateFailed(NoConnectionException innerException) {
        this((Throwable) innerException);
    }

    public boolean invalidToken() {
        if (!(innerException instanceof HttpException))
            return false;
        HttpException exception = (HttpException) innerException;
        return exception.response() != null && exception.response().code() == sInvalidToken;
    }
}
