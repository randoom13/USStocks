package amber.random.com.usstocks.database;

import android.database.Cursor;

import java.util.Collection;
import java.util.Map;

import amber.random.com.usstocks.models.Company;
import amber.random.com.usstocks.models.Indicator;

public interface DataBaseHelperProxy {

    //region company table columns

    String COMPANY_ID = "company_id";
    String COMPANY_NAME = "company_name";
    String COMPANY_PREVIOUS_NAMES = "company_old_names";

    //endregion company table columns

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
