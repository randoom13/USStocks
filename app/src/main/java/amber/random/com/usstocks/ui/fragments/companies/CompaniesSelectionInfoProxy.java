package amber.random.com.usstocks.ui.fragments.companies;

import android.support.v4.os.CancellationSignal;

import java.util.HashMap;
import java.util.Map;

import amber.random.com.usstocks.database.DataBaseHelperProxy;
import amber.random.com.usstocks.injection.App;
import amber.random.com.usstocks.ui.fragments.base.BaseSelectionInfoProxy;

public class CompaniesSelectionInfoProxy extends BaseSelectionInfoProxy {
    public CompaniesSelectionInfoProxy(int maxCacheSize) {
        super(maxCacheSize);
        App.getRequestComponent().inject(this);
    }

    @Override
    protected Map<Integer, Boolean> getSyncCheckedInfo(DataBaseHelperProxy database,
                                                       boolean resetSelection,
                                                       Map<Integer, Boolean> checkedCache,
                                                       CancellationSignal cancellation) {
        Map<Integer, Boolean> syncCheckedCache = new HashMap<Integer, Boolean>();
        if (resetSelection)
            database.unSelectCompanies(mFilter);

        database.checkCompaniesById(checkedCache);
        syncCheckedCache = database.getCheckCompaniesById(checkedCache);
        cancellation.throwIfCanceled();
        return syncCheckedCache;
    }
}