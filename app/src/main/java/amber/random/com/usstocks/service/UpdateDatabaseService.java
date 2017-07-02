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

import javax.inject.Inject;

import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.exceptions.UpdateFailed;
import amber.random.com.usstocks.injection.App;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateDatabaseService extends Service {

    //region intent constants
    public final static String UPDATE_COMPLETED = "update_completed";
    public final static String COMPANIES_LIST = "companies_list";
    public final static String COMPANY_INDICATOR_LIST = "company_indicators_list";
    public final static String INDICATORS_LIST = "indicators_list";
    public final static String EXTRA_DATA_UPDATE = "update";
    public final static String EXTRA_DATA_ERROR = "error";
    public final static String EXTRA_TOKEN = "token";
    @Inject
    protected DataBaseHelper mDataBaseHelper;
    //endregion intent constants
    @Inject
    protected BackendService mBackendService;
    private Disposable mDisposable;

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
            getCompaniesList(token);
        else if (extra.equals(INDICATORS_LIST)) {
            getIndicatorsList(token);
        } else {
            Log.d(getClass().getSimpleName(), "Unknown command");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            mDisposable = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sentIntent(String operationName, Throwable exception) {
        Intent intent = new Intent(UPDATE_COMPLETED);
        intent.putExtra(EXTRA_DATA_UPDATE, operationName);
        if (exception != null)
            intent.putExtra(EXTRA_DATA_ERROR, new UpdateFailed(exception));
        LocalBroadcastManager.getInstance(UpdateDatabaseService.this).sendBroadcast(intent);
    }

    private void getCompaniesList(String token) {
        ((App) getApplication()).getRequestComponent().inject(this);
        mDisposable = mBackendService.getAllCompanies(token)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .onErrorReturn(ex -> {
                    sentIntent(COMPANIES_LIST, ex);
                    return null;
                })
                .subscribe(companies ->
                {
                    if (companies != null) {
                        mDataBaseHelper.addCompanies(companies);
                        sentIntent(COMPANIES_LIST, null);
                        stopSelf();
                    }
                }, er -> {
                    sentIntent(COMPANIES_LIST, er);
                    stopSelf();
                });
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    private void getIndicatorsList(String token) {
        ((App) getApplication()).getRequestComponent().inject(this);
        mDisposable = mBackendService.getAllIndicators(token)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .onErrorReturn(ex -> {
                    sentIntent(INDICATORS_LIST, ex);
                    return null;
                })
                .subscribe(indicators ->
                {
                    if (indicators != null) {
                        mDataBaseHelper.addIndicators(indicators);
                        sentIntent(INDICATORS_LIST, null);
                        stopSelf();
                    }
                }, er -> {
                    sentIntent(INDICATORS_LIST, er);
                    stopSelf();
                });
    }
}
