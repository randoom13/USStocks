package amber.random.com.usstocks.injection;

import amber.random.com.usstocks.service.UpdateDatabaseService;
import amber.random.com.usstocks.ui.MainActivity;
import amber.random.com.usstocks.ui.fragments.TokenDialogFragment;
import amber.random.com.usstocks.ui.fragments.companies.CompaniesCursorAdapter;
import amber.random.com.usstocks.ui.fragments.companies.CompaniesFragment;
import amber.random.com.usstocks.ui.fragments.companies.CompaniesSelectionInfoProxy;
import amber.random.com.usstocks.ui.fragments.companies_details.CompaniesDetailsFragment;

public interface RequestComponent {
    void inject(UpdateDatabaseService service);

    void inject(CompaniesDetailsFragment detailsFragment);

    void inject(CompaniesFragment companiesFragment);

    void inject(CompaniesSelectionInfoProxy syncWithDataBaseRunnable);

    void inject(MainActivity mainActivity);

    void inject(TokenDialogFragment tokenDialogFragment);

    void inject(CompaniesCursorAdapter companiesCursorAdapter);
}
