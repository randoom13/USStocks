package amber.random.com.usstocks.restdata;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public abstract class CommonGetRestData<T> extends Thread {

    private static String mUrlQuery;
    public CommonGetRestData(String urlQuery) {
        mUrlQuery = urlQuery;
    }
    private static final String TOKEN = "36h9GzLD1Vz3avW2Mibvmg";

    protected abstract void processData(T result);

    protected abstract T parseJsonTo(BufferedReader reader) throws IOException;

    @Override
    public void run() {
        T result = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(String.format(mUrlQuery, TOKEN)).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Reader incoming = response.body().charStream();
                BufferedReader reader = new BufferedReader(incoming);
                result  = parseJsonTo(reader);
                reader.close();
            } else
            {
                Log.e(getClass().getSimpleName(), response.toString());
                return;
            }
        } catch (Exception ex) {
            Log.e(getClass().getSimpleName(), "Can't parse json : ", ex);
            return;
        }
        if (result != null)
            processData(result);
    }
}
