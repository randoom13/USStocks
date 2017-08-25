package amber.random.com.usstocks.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import javax.inject.Inject;

import amber.random.com.usstocks.App;
import amber.random.com.usstocks.database.DataBaseHelperProxy;
import amber.random.com.usstocks.exceptions.NoConnectionException;
import amber.random.com.usstocks.exceptions.UnknownFormatException;
import amber.random.com.usstocks.exceptions.UpdateFailedException;
import amber.random.com.usstocks.preference.AppPreferences;
import amber.random.com.usstocks.service.rest.BackendServiceProxy;
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

    //endregion intent constants

    @Inject
    protected DataBaseHelperProxy mDataBaseHelper;
    @Inject
    protected BackendServiceProxy mBackendService;
    @Inject
    protected AppPreferences mAppPreferences;

    private Disposable mDisposable;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        App.getRequestComponent().inject(this);
        String tableName = intent.getStringExtra(EXTRA_DATA_UPDATE);
        if (COMPANIES_LIST.equals(tableName)) {
            getCompaniesList(startId);
        } else if (INDICATORS_LIST.equals(tableName)) {
            getIndicatorsList(startId);
        } else if (!TextUtils.isEmpty(tableName))
            Log.w(getClass().getSimpleName(), " Update for " + tableName + " is not implemented!");

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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE));
        return null != connectivityManager.getActiveNetworkInfo() && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void getCompaniesList(Integer startId) {
        if (!isNetworkAvailable()) {
            sentIntent(COMPANIES_LIST, new UpdateFailedException(new NoConnectionException()));
            stopSelf(startId);
        } else
            mDisposable = mBackendService.getAllCompanies(mAppPreferences.getToken())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.io())
                    .subscribe(companies ->
                            {
                                mDataBaseHelper.addCompanies(companies);
                                sentIntent(COMPANIES_LIST, null);
                            }, ex -> handleException(COMPANIES_LIST, ex),
                            () -> stopSelf(startId));
    }

    @Override
    public void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();

        super.onDestroy();
    }

    private void handleException(String tableName, Throwable ex) {
        if (ex instanceof UnknownFormatException) {
            Log.e(getClass().getSimpleName(), "Failed to load the " + tableName + " from internet", ex);
            sentIntent(tableName, new UpdateFailedException((UnknownFormatException) ex));
        } else if (ex instanceof HttpException) {
            Log.e(getClass().getSimpleName(), "Failed to load the " + tableName + " from internet", ex);
            sentIntent(tableName, new UpdateFailedException((HttpException) ex));
        } else if (ex instanceof SQLException) {
            Log.e(getClass().getSimpleName(), "Failed to save " + tableName + " in database", ex);
            sentIntent(tableName, new UpdateFailedException((SQLException) ex));
        } else {
            Log.e(getClass().getSimpleName(), "Unhandled exception during update " + tableName, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void getIndicatorsList(int startId) {
        if (!isNetworkAvailable()) {
            sentIntent(INDICATORS_LIST, new UpdateFailedException(new NoConnectionException()));
            stopSelf(startId);
        } else
            mDisposable = mBackendService.getAllIndicators(mAppPreferences.getToken())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.io())
                    .subscribe(indicators ->
                            {
                                mDataBaseHelper.addIndicators(indicators);
                                sentIntent(INDICATORS_LIST, null);
                            }, ex -> handleException(INDICATORS_LIST, ex),
                            () -> stopSelf(startId));
    }
}
