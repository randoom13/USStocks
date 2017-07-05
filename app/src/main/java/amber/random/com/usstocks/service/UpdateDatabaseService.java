package amber.random.com.usstocks.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import javax.inject.Inject;

import amber.random.com.usstocks.database.DataBaseHelperProxy;
import amber.random.com.usstocks.exceptions.UpdateFailed;
import amber.random.com.usstocks.injection.App;
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
    public final static String EXTRA_TOKEN = "token";

    //endregion intent constants

    @Inject
    protected DataBaseHelperProxy mDataBaseHelper;
    @Inject
    protected BackendServiceProxy mBackendService;
    @Inject
    protected SharedPreferences mSharedPreferences;

    private Disposable mDisposable;

    private String getToken(Intent intent) {
        String tokenKey = intent.getStringExtra(EXTRA_TOKEN);
        String token = "";
        if (!TextUtils.isEmpty(tokenKey)) {
            token = mSharedPreferences.getString(tokenKey, "");
        }
        return token;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((App) getApplication()).getRequestComponent().inject(this);
        String commandName = intent.getStringExtra(EXTRA_DATA_UPDATE);
        if (TextUtils.isEmpty(commandName))
            return START_NOT_STICKY;

        if (commandName.equals(COMPANIES_LIST))
            getCompaniesList(getToken(intent), startId);
        else if (commandName.equals(INDICATORS_LIST)) {
            getIndicatorsList(getToken(intent), startId);
        } else {
            Log.d(getClass().getSimpleName(), "Unknown command: " + commandName);
        }

        return START_NOT_STICKY;
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

    private void getCompaniesList(String token, int startId) {
        mDisposable = mBackendService.getAllCompanies(token)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .onErrorReturn(ex -> {
                    Log.d(getClass().getSimpleName(), "Can't get companies list", ex);
                    sentIntent(COMPANIES_LIST, ex);
                    return null;
                })
                .subscribe(companies ->
                {
                    if (companies != null) {
                        mDataBaseHelper.addCompanies(companies);
                        sentIntent(COMPANIES_LIST, null);
                    }
                }, ex -> {
                    Log.d(getClass().getSimpleName(), "Can't save companies list in database", ex);
                    sentIntent(COMPANIES_LIST, ex);
                }, () -> {
                    stopSelf(startId);
                });
    }

    @Override
    public void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();

        super.onDestroy();
    }

    private void getIndicatorsList(String token, int startId) {
        mDisposable = mBackendService.getAllIndicators(token)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .onErrorReturn(ex -> {
                    Log.d(getClass().getSimpleName(), "Can't get indicators list", ex);
                    sentIntent(INDICATORS_LIST, ex);
                    return null;
                })
                .subscribe(indicators ->
                {
                    if (indicators != null) {
                        mDataBaseHelper.addIndicators(indicators);
                        sentIntent(INDICATORS_LIST, null);
                    }
                }, ex -> {
                    Log.d(getClass().getSimpleName(), "Can't save indicators list in database", ex);
                    sentIntent(INDICATORS_LIST, ex);
                }, () -> {
                    stopSelf(startId);
                });
    }
}
