package amber.random.com.usstocks.injection;

import javax.inject.Singleton;

import amber.random.com.usstocks.fragments.companies.CompaniesFragment;
import amber.random.com.usstocks.fragments.companies.CompaniesSelectionInfoProxy;
import amber.random.com.usstocks.fragments.companies_details.CompaniesDetailsPagerFragment;
import amber.random.com.usstocks.injection.modules.AppModule;
import amber.random.com.usstocks.injection.modules.NetworkModule;
import amber.random.com.usstocks.injection.modules.RequestModule;
import amber.random.com.usstocks.service.UpdateDatabaseService;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RequestModule.class, NetworkModule.class})
public interface RequestComponent {
    void inject(UpdateDatabaseService service);

    void inject(CompaniesFragment companiesFragment);

    void inject(CompaniesSelectionInfoProxy syncWithDataBaseRunnable);

    void inject(CompaniesDetailsPagerFragment.ThreadLoadCursor threadLoadCursor);
}
