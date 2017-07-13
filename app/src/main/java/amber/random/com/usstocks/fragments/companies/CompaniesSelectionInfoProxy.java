package amber.random.com.usstocks.fragments.companies;

import android.support.v4.os.CancellationSignal;

import java.util.HashMap;
import java.util.Map;

import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.fragments.base.BaseSelectionInfoProxy;
import amber.random.com.usstocks.injection.App;

public class CompaniesSelectionInfoProxy extends BaseSelectionInfoProxy {
    public CompaniesSelectionInfoProxy(int maxCacheSize) {
        super(maxCacheSize);
        App.getRequestComponent().inject(this);
    }

    @Override
    protected Map<Integer, Boolean> getSyncCheckedInfo(DataBaseHelper database,
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
