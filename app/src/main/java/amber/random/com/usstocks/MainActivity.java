package amber.random.com.usstocks;


import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;

import amber.random.com.usstocks.fragments.companies.CompaniesListFragment;
import amber.random.com.usstocks.fragments.companies_details.CompaniesDetailsFragment;
import amber.random.com.usstocks.fragments.companies_details.PrimaryCompaniesDetailsFragment;


public class MainActivity extends AppCompatActivity implements CompaniesListFragment.Contract,
        CompaniesDetailsFragment.Contract {
    public static final String COMPANIES_TAG = "companies_tag";
    public static final String COMPANY_DETAILS_TAG = "company_details_tag";
    private CompaniesListFragment mCompaniesFragment = null;
    private PrimaryCompaniesDetailsFragment mCompaniesDetailsFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupStrictMode();
        mCompaniesFragment = (CompaniesListFragment) getSupportFragmentManager()
                .findFragmentByTag(COMPANIES_TAG);
        mCompaniesDetailsFragment = (PrimaryCompaniesDetailsFragment) getSupportFragmentManager()
                .findFragmentByTag(COMPANY_DETAILS_TAG);
        if (mCompaniesFragment == null) {
            mCompaniesFragment = new CompaniesListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mainfrag, mCompaniesFragment, COMPANIES_TAG)
                    .commit();
        }
        if (mCompaniesDetailsFragment == null) {
            mCompaniesDetailsFragment = new PrimaryCompaniesDetailsFragment();
        }
    }

    public void showDetails(String filter) {
        mCompaniesDetailsFragment = PrimaryCompaniesDetailsFragment.newInstance(filter);
        getSupportFragmentManager().beginTransaction().hide(mCompaniesFragment)
                .addToBackStack(null)
                .add(R.id.mainfrag, mCompaniesDetailsFragment, COMPANY_DETAILS_TAG)
                .commit();
    }

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
