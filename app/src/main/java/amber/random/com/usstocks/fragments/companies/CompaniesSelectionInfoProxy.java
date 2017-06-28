package amber.random.com.usstocks.fragments.companies;

import android.app.Activity;
import android.support.v4.os.CancellationSignal;

import java.util.HashMap;
import java.util.Map;

import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.fragments.base.BaseSelectionInfoProxy;
import amber.random.com.usstocks.injection.App;

public class CompaniesSelectionInfoProxy extends BaseSelectionInfoProxy {
    public CompaniesSelectionInfoProxy(int maxCacheSize, Activity activity) {
        super(maxCacheSize, activity);
    }

    @Override
    protected void launchDatabaseSync(boolean isAsync, boolean resetSelection,
                                      CancellationSignal cancellation,
                                      BaseSelectionInfoProxy.SyncCompletedCallback callback) {
        SyncWithDataBaseRunnable runnable =
                new SyncWithDataBaseRunnable(isAsync, cancellation, resetSelection);
        runnable.addCompletedListener(callback);
        if (isAsync)
            new Thread(runnable).start();
        else runnable.run();
    }

    public class SyncWithDataBaseRunnable extends BaseSyncWithDataBaseRunnable {

        public SyncWithDataBaseRunnable(boolean isHandleException, CancellationSignal cancellation,
                                        boolean resetSelection) {
            super(isHandleException, cancellation, resetSelection);
        }

        @Override
        public void run() {
            ((App) mActivity.getApplication()).getRequestComponent().inject(this);
            super.run();
        }

        @Override
        protected Map<Integer, Boolean> getSyncCheckedinfo(DataBaseHelper database,
                                                           Map<Integer, Boolean> checkedCache) {
            Map<Integer, Boolean> syncCheckedCache = new HashMap<Integer, Boolean>();
            if (mResetSelection)
                database.unSelectCompanies(mFilter);

            database.checkCompaniesById(checkedCache);
            syncCheckedCache = database.getCheckCompaniesById(checkedCache);
            mCancellation.throwIfCanceled();
            return syncCheckedCache;
        }
    }
}
