package amber.random.com.usstocks.injection.modules;

import android.app.Application;

import javax.inject.Singleton;

import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.database.DataBaseHelperProxy;
import dagger.Module;
import dagger.Provides;

@Module
public class RequestModule {

    @Provides
    @Singleton
    DataBaseHelper DataBaseHelper(Application application) {
        return new DataBaseHelper(application);
    }

    @Provides
    @Singleton
    DataBaseHelperProxy getDataBaseHelper(Application application) {
        return new DataBaseHelper(application);
    }
}

