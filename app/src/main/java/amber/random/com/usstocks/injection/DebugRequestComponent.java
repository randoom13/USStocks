package amber.random.com.usstocks.injection;

import javax.inject.Singleton;

import amber.random.com.usstocks.injection.modules.AppModule;
import amber.random.com.usstocks.injection.modules.FragmentsModule;
import amber.random.com.usstocks.injection.modules.NetworkModule;
import amber.random.com.usstocks.injection.modules.RequestModule;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RequestModule.class,
        NetworkModule.class, FragmentsModule.class})
public interface DebugRequestComponent extends RequestComponent {
}
