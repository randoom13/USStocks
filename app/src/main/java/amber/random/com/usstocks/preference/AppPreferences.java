package amber.random.com.usstocks.preference;


import io.reactivex.Observable;

public interface AppPreferences {

    String getToken();

    void setToken(String token);

    Observable<Boolean> hasToken();
}
