package amber.random.com.usstocks.preference;


public interface AppPreferences {
    String getToken();

    void setToken(String token);

    boolean hasToken();
}
