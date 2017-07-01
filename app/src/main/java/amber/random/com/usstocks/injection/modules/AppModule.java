package amber.random.com.usstocks.injection.modules;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    @Provides
    @Singleton
    public SharedPreferences provides(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }
}
