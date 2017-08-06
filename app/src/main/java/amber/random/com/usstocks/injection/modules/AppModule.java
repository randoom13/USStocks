package amber.random.com.usstocks.injection.modules;

import android.app.Application;

import javax.inject.Singleton;

import amber.random.com.usstocks.preference.AppPreferences;
import amber.random.com.usstocks.preference.AppPreferencesImpl;
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

    @Provides
    @Singleton
    public AppPreferences provides(Application app) {
        return new AppPreferencesImpl(app);
    }
}
