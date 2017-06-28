package amber.random.com.usstocks.injection;

import javax.inject.Singleton;

import amber.random.com.usstocks.fragments.companies.CompaniesFragment;
import amber.random.com.usstocks.fragments.companies.CompaniesSelectionInfoProxy;
import amber.random.com.usstocks.fragments.companies_details.CompaniesDetailsPagerFragment;
import amber.random.com.usstocks.service.UpdateDatabaseService;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RequestModule.class})
public interface RequestComponent {
    void inject(UpdateDatabaseService.GetCompaniesList getCompaniesList);

    void inject(UpdateDatabaseService.GetIndicatorsList getIndicatorsList);

    void inject(CompaniesFragment.LoadCompaniesList loadCompaniesList);

    void inject(CompaniesSelectionInfoProxy.SyncWithDataBaseRunnable syncWithDataBaseRunnable);

    void inject(CompaniesDetailsPagerFragment.ThreadLoadCursor threadLoadCursor);
}
