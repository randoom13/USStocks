package amber.random.com.usstocks.injection.modules;

import amber.random.com.usstocks.ui.fragments.base.SelectionInfoProxyCapable;
import amber.random.com.usstocks.ui.fragments.companies.CompaniesSelectionInfoProxy;
import dagger.Module;
import dagger.Provides;

@Module
public class FragmentsModule {
    @Provides
    public SelectionInfoProxyCapable providesSelectionInfoProxy() {
        return new CompaniesSelectionInfoProxy(300);
    }
}
