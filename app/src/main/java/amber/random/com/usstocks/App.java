package amber.random.com.usstocks;

import android.app.Application;
import android.support.annotation.VisibleForTesting;

import amber.random.com.usstocks.injection.DaggerDebugRequestComponent;
import amber.random.com.usstocks.injection.DebugRequestComponent;
import amber.random.com.usstocks.injection.RequestComponent;
import amber.random.com.usstocks.injection.modules.AppModule;
import amber.random.com.usstocks.injection.modules.FragmentsModule;
import amber.random.com.usstocks.injection.modules.NetworkModule;
import amber.random.com.usstocks.injection.modules.RequestModule;

public class App extends Application {
    private static RequestComponent mRequestComponent;

    public static RequestComponent getRequestComponent() {
        return mRequestComponent;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setRequestComponent(RequestComponent requestComponent) {
        mRequestComponent = requestComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DebugRequestComponent requestComponent = DaggerDebugRequestComponent.builder()
                .appModule(new AppModule(this))
                .requestModule(new RequestModule())
                .networkModule(new NetworkModule())
                .fragmentsModule(new FragmentsModule())
                .build();
        mRequestComponent = requestComponent;
    }
}
