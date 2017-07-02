package amber.random.com.usstocks;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import amber.random.com.usstocks.fragments.TokenDialogFragment;
import amber.random.com.usstocks.fragments.companies.CompaniesFragment;
import amber.random.com.usstocks.fragments.companies_details.CompaniesDetailsFragment;


public class MainActivity extends AppCompatActivity implements CompaniesFragment.Contract,
        TokenDialogFragment.TokenDialogListener {
    public static final String COMPANIES_TAG = "companies_tag";
    public static final String COMPANY_DETAILS_TAG = "company_details_tag";
    public static final String TOKEN_KEY = "unique_token";
    public static final String TOKEN_TAG = "token_dialog_tag";
    private CompaniesFragment mCompaniesFragment = null;
    private CompaniesDetailsFragment mCompaniesDetailsFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupStrictMode();
        mCompaniesFragment = (CompaniesFragment) getSupportFragmentManager()
                .findFragmentByTag(COMPANIES_TAG);
        mCompaniesDetailsFragment = (CompaniesDetailsFragment) getSupportFragmentManager()
                .findFragmentByTag(COMPANY_DETAILS_TAG);
        verifyLiveToken();
    }

    @Override
    public void onClick(boolean isClose) {
        if (isClose)
            finish();

        verifyLiveToken();
    }

    private void verifyLiveToken() {
        if (hasToken())
            initializeFragments();
        else
            showTokenDialog(getString(R.string.token_dialog_desc), this);
    }

    private void initializeFragments() {
        if (mCompaniesFragment == null) {
            mCompaniesFragment = new CompaniesFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mainfrag, mCompaniesFragment, COMPANIES_TAG)
                    .commit();
        }

        if (mCompaniesDetailsFragment == null) {
            mCompaniesDetailsFragment = new CompaniesDetailsFragment();
        }
    }

    //region CompaniesFragment.Contract implementation
    @Override
    public boolean hasToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext());
        String token = prefs.getString(TOKEN_KEY, "");
        return !TextUtils.isEmpty(token);
    }

    @Override
    public String getTokenKey() {
        return TOKEN_KEY;
    }

    @Override
    public void showDetails(String filter) {
        mCompaniesDetailsFragment = CompaniesDetailsFragment.newInstance(filter);
        getSupportFragmentManager().beginTransaction().hide(mCompaniesFragment)
                .addToBackStack(null)
                .add(R.id.mainfrag, mCompaniesDetailsFragment, COMPANY_DETAILS_TAG)
                .commit();
    }


    @Override
    public void showTokenDialog(String desc, TokenDialogFragment.TokenDialogListener listener) {
        TokenDialogFragment dialogFragment = TokenDialogFragment.newInstance(TOKEN_KEY, desc);
        if (listener != null)
            dialogFragment.addClickListener(listener);

        dialogFragment.show(getSupportFragmentManager(), TOKEN_TAG);
    }

    //endregion CompaniesFragment.Contract implementation

    private void setupStrictMode() {
        StrictMode.ThreadPolicy.Builder builder =
                new StrictMode.ThreadPolicy.Builder()
                        .detectAll().penaltyLog();
        if (BuildConfig.DEBUG) {
            builder.penaltyFlashScreen();
        }
        StrictMode.setThreadPolicy(builder.build());
    }
}
