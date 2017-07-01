package amber.random.com.usstocks.service;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.inject.Inject;

import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.exceptions.UpdateFailed;

public abstract class CommonGetRestData<T> extends Thread {

    private static String mUrlQuery;
    protected UpdateFailed mError;
    @Inject
    protected DataBaseHelper mDataBaseHelper;
    @Inject
    protected BackendService mBackendService;

    public CommonGetRestData(String urlQuery) {
        mUrlQuery = urlQuery;
    }

    protected abstract void processData(T result);

    protected abstract T parseJsonTo(BufferedReader reader) throws IOException;

    @Override
    public void run() {
        T result = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(mUrlQuery).build();
            Response response = client.newCall(request).execute();
            Reader incoming = response.body().charStream();
            BufferedReader reader = new BufferedReader(incoming);
            if (response.isSuccessful()) {
                result = parseJsonTo(reader);
                reader.close();
            }
        } catch (Exception ex) {
            mError = new UpdateFailed(ex);
            Log.e(getClass().getSimpleName(), "Can't parse json : ", ex);
            return;
        }

        if (result != null)
            processData(result);
    }
}
