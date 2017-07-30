package amber.random.com.usstocks.injection;

import amber.random.com.usstocks.MainActivity;
import amber.random.com.usstocks.fragments.TokenDialogFragment;
import amber.random.com.usstocks.fragments.companies.CompaniesFragment;
import amber.random.com.usstocks.fragments.companies.CompaniesSelectionInfoProxy;
import amber.random.com.usstocks.fragments.companies_details.CompaniesDetailsFragment;
import amber.random.com.usstocks.service.UpdateDatabaseService;

public interface RequestComponent {
    void inject(UpdateDatabaseService service);

    void inject(CompaniesDetailsFragment detailsFragment);

    void inject(CompaniesFragment companiesFragment);

    void inject(CompaniesSelectionInfoProxy syncWithDataBaseRunnable);

    void inject(MainActivity mainActivity);

    void inject(TokenDialogFragment tokenDialogFragment);
}
