package amber.random.com.usstocks.injection;

import javax.inject.Singleton;

import amber.random.com.usstocks.injection.modules.AppModule;
import amber.random.com.usstocks.injection.modules.FragmentsModule;
import amber.random.com.usstocks.injection.modules.NetworkModule;
import amber.random.com.usstocks.injection.modules.RequestModule;
import amber.random.com.usstocks.service.UpdateDatabaseService;
import amber.random.com.usstocks.ui.MainActivity;
import amber.random.com.usstocks.ui.fragments.TokenDialogFragment;
import amber.random.com.usstocks.ui.fragments.companies.CompaniesCursorAdapter;
import amber.random.com.usstocks.ui.fragments.companies.CompaniesFragment;
import amber.random.com.usstocks.ui.fragments.companies.CompaniesSelectionInfoProxy;
import amber.random.com.usstocks.ui.fragments.companies_details.CompaniesDetailsFragment;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RequestModule.class,
        NetworkModule.class, FragmentsModule.class})
public interface RequestComponent {
    void inject(UpdateDatabaseService service);

    void inject(CompaniesDetailsFragment detailsFragment);

    void inject(CompaniesFragment companiesFragment);

    void inject(CompaniesSelectionInfoProxy syncWithDataBaseRunnable);

    void inject(MainActivity mainActivity);

    void inject(TokenDialogFragment tokenDialogFragment);

    void inject(CompaniesCursorAdapter companiesCursorAdapter);
}
