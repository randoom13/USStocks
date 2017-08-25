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
    public DataBaseHelperProxy providesDataBaseHelper(Application application) {
        return new DataBaseHelper(application);
    }
}

