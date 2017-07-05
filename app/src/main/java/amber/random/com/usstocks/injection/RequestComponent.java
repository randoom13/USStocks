package amber.random.com.usstocks.injection;

import javax.inject.Singleton;

import amber.random.com.usstocks.MainActivity;
import amber.random.com.usstocks.fragments.TokenDialogFragment;
import amber.random.com.usstocks.fragments.companies.CompaniesFragment;
import amber.random.com.usstocks.fragments.companies.CompaniesSelectionInfoProxy;
import amber.random.com.usstocks.fragments.companies_details.CompaniesDetailsFragment;
import amber.random.com.usstocks.injection.modules.AppModule;
import amber.random.com.usstocks.injection.modules.NetworkModule;
import amber.random.com.usstocks.injection.modules.RequestModule;
import amber.random.com.usstocks.service.UpdateDatabaseService;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RequestModule.class, NetworkModule.class})
public interface RequestComponent {
    void inject(UpdateDatabaseService service);

    void inject(CompaniesDetailsFragment detailsFragment);

    void inject(CompaniesFragment companiesFragment);

    void inject(CompaniesSelectionInfoProxy syncWithDataBaseRunnable);

    void inject(MainActivity mainActivity);

    void inject(TokenDialogFragment tokenDialogFragment);
}
