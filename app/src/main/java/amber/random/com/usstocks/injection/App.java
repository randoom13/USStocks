package amber.random.com.usstocks.injection;

import android.app.Application;

public class App extends Application {
    private RequestComponent mRequestComponent;

    public RequestComponent getRequestComponent() {
        return mRequestComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestComponent = DaggerRequestComponent.builder()
                .appModule(new AppModule(this))
                .requestModule(new RequestModule())
                .build();
    }
}
