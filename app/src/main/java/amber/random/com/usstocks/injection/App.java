package amber.random.com.usstocks.injection;

import android.app.Application;

import amber.random.com.usstocks.injection.modules.AppModule;
import amber.random.com.usstocks.injection.modules.NetworkModule;
import amber.random.com.usstocks.injection.modules.RequestModule;

public class App extends Application {
    private static RequestComponent mRequestComponent;

    public static RequestComponent getRequestComponent() {
        return mRequestComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestComponent = DaggerRequestComponent.builder()
                .appModule(new AppModule(this))
                .requestModule(new RequestModule())
                .networkModule(new NetworkModule())
                .build();
    }
}
