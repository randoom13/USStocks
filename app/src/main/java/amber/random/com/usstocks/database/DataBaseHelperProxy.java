package amber.random.com.usstocks.database;

import android.database.Cursor;

import java.util.Collection;
import java.util.Map;

import amber.random.com.usstocks.models.Company;
import amber.random.com.usstocks.models.Indicator;

public interface DataBaseHelperProxy {

    Cursor getSelectedCompanies(String filter);

    Cursor getCompaniesCheckedState(String filter);

    Map<Integer, Boolean> getCheckCompaniesById(Map<Integer, Boolean> checkedCache);

    void unSelectCompanies(String filter);

    void checkCompaniesById(Map<Integer, Boolean> checkedCache);

    int getMaxId(String filter);

    void addIndicators(Collection<Indicator> companies);

    void addCompanies(Collection<Company> companies);

    Cursor getCompanies(String filter);
}
