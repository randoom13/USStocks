package amber.random.com.usstocks.exceptions;

import java.io.IOException;

public class NoConnectionException extends IOException {
    @Override
    public String getMessage() {
        return "No network available, please check your WiFi or Data connection";
    }
}
