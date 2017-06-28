package amber.random.com.usstocks.injection;

import android.app.Application;

import javax.inject.Singleton;

import amber.random.com.usstocks.database.DataBaseHelper;
import dagger.Module;
import dagger.Provides;

@Module
public class RequestModule {

    @Provides
    @Singleton
    DataBaseHelper DataBaseHelper(Application application) {
        return new DataBaseHelper(application);
    }
}
