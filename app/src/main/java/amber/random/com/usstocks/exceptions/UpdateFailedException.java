package amber.random.com.usstocks.exceptions;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import retrofit2.Response;

public class UpdateFailedException extends Exception {
    private static final int sInvalidToken = 403;

    public final Throwable innerException;

    private UpdateFailedException(Throwable exception) {
        innerException = exception;
    }

    public UpdateFailedException(UnknownFormatException innerException) {
        this((Throwable) innerException);
    }

    public UpdateFailedException(HttpException innerException) {
        this((Throwable) innerException);
    }

    public UpdateFailedException(android.database.SQLException innerException) {
        this((Throwable) innerException);
    }


    public UpdateFailedException(NoConnectionException innerException) {
        this((Throwable) innerException);
    }

    public boolean invalidToken() {
        if (!(innerException instanceof HttpException))
            return false;
        HttpException exception = (HttpException) innerException;
        Response response = exception.response();
        return null != response && response.code() == sInvalidToken;
    }
}
