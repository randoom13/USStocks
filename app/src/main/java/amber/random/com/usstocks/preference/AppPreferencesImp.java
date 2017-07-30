package amber.random.com.usstocks.preference;


import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class AppPreferencesImp implements AppPreferences {
    private static final String sTokenKey = "unique_token";
    private SharedPreferences mSharedPreferences;

    public AppPreferencesImp(Application app) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Override
    public String getToken() {
        String token = mSharedPreferences.getString(sTokenKey, "");
        return token;
    }

    @Override
    public void setToken(String token) {
        mSharedPreferences.edit().putString(sTokenKey, token).commit();
    }

    @Override
    public boolean hasToken() {
        return !TextUtils.isEmpty(getToken());
    }
}
