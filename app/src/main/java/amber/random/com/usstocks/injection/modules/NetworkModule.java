package amber.random.com.usstocks.injection.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import javax.inject.Singleton;

import amber.random.com.usstocks.BuildConfig;
import amber.random.com.usstocks.models.AutoValueGsonFactory;
import amber.random.com.usstocks.service.rest.BackendServiceProxy;
import amber.random.com.usstocks.service.rest.BackendServiceProxyImpl;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkModule {
    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().serializeNulls().
                registerTypeAdapterFactory(AutoValueGsonFactory.create()).create();
    }

    @Provides
    @Singleton
    GsonConverterFactory providesGsonConverterFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(GsonConverterFactory factory) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BuildConfig.END_POINT)
                .addConverterFactory(factory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit;
    }

    @Provides
    @Singleton
    BackendServiceProxy getBackendService() {
        return new BackendServiceProxyImpl();
    }
}
