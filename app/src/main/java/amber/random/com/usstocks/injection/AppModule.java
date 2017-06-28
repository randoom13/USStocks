package amber.random.com.usstocks.injection;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private Application mMainApplication;

    public AppModule(Application mainApplication) {
        mMainApplication = mainApplication;
    }

    @Provides
    @Singleton
    public Application getApplication() {
        return mMainApplication;
    }
}
