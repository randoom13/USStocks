package amber.random.com.usstocks.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import amber.random.com.usstocks.injection.App;
import amber.random.com.usstocks.models.Company;
import amber.random.com.usstocks.models.Indicator;

public class UpdateDatabaseService extends Service {

    //region intent constants

    public final static String UPDATE_COMPLETED = "update_completed";
    public final static String COMPANIES_LIST = "companies_list";
    public final static String COMPANY_INDICATOR_LIST = "company_indicators_list";
    public final static String INDICATORS_LIST = "indicators_list";
    public final static String EXTRA_DATA_UPDATE = "update";
    public final static String EXTRA_DATA_ERROR = "error";
    public final static String EXTRA_TOKEN = "token";

    //endregion intent constants

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String tokenKey = intent.getStringExtra(EXTRA_TOKEN);
        String token = "";
        if (!TextUtils.isEmpty(tokenKey)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                    (getApplicationContext());
            token = prefs.getString(tokenKey, "");
        }

        String extra = intent.getStringExtra(EXTRA_DATA_UPDATE);
        if (extra.equals(COMPANIES_LIST))
            new GetCompaniesList(token).start();
        else if (extra.equals(INDICATORS_LIST)) {
            new GetIndicatorsList(token).start();
        } else {
            Log.d(getClass().getSimpleName(), "Unknown command");
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sentIntent(String operationName, Exception exception) {
        Intent intent = new Intent(UPDATE_COMPLETED);
        intent.putExtra(EXTRA_DATA_UPDATE, operationName);
        if (exception != null)
            intent.putExtra(EXTRA_DATA_ERROR, exception);

        LocalBroadcastManager.getInstance(UpdateDatabaseService.this).sendBroadcast(intent);
    }

    public class GetCompaniesList extends CommonGetRestData<Collection<Company>> {
        public GetCompaniesList(String token) {
            super(RestServiceRequestHelper.getAllCompanies(token));
        }

        @Override
        public void run() {
            ((App) getApplication()).getRequestComponent().inject(this);
            super.run();
            if (mError != null) {
                sentIntent(COMPANIES_LIST, mError);
                stopSelf();
            }
        }

        @Override
        protected Collection<Company> parseJsonTo(BufferedReader reader) {
            Type collectionType = new TypeToken<Collection<Company>>() {
            }.getType();
            Collection<Company> result = new Gson().fromJson(reader, collectionType);
            return result;
        }

        @Override
        protected void processData(Collection<Company> result) {
            try {
                mDataBaseHelper.addCompanies(result);
                sentIntent(COMPANIES_LIST, null);
            } catch (Exception ex) {
                Log.e(getClass().getSimpleName(), "Can't updata companies in database", ex);
                sentIntent(COMPANIES_LIST, mError);
            }

            stopSelf();
        }
    }

    public class GetIndicatorsList extends CommonGetRestData<Collection<Indicator>> {
        public GetIndicatorsList(String token) {
            super(RestServiceRequestHelper.getAllIndicators(token));
        }

        @Override
        public void run() {
            ((App) getApplication()).getRequestComponent().inject(this);
            super.run();
            if (mError != null) {
                sentIntent(COMPANIES_LIST, mError);
                stopSelf();
            }
        }

        @Override
        protected void processData(Collection<Indicator> result) {
            try {
                mDataBaseHelper.addIndicators(result);
                sentIntent(COMPANIES_LIST, null);
            } catch (Exception ex) {
                Log.e(getClass().getSimpleName(), "Can't updata indicators in database", ex);
                sentIntent(COMPANIES_LIST, ex);
            }
            stopSelf();
        }

        private Integer tryParse(String string) {
            try {
                return Integer.valueOf(string);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        @Override
        protected Collection<Indicator> parseJsonTo(BufferedReader reader) throws IOException {
            List<Indicator> indicators = new ArrayList<Indicator>();
            String line = "";
            while ((line = reader.readLine()) != null) {
                List<String> names = new ArrayList<String>();
                for (String item : line.split(",")) {
                    Integer number = tryParse(item);
                    if (number == null)
                        names.add(item);
                    else indicators.add(new Indicator(number, names));
                }
            }
            return indicators;
        }
    }
}
