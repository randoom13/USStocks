package amber.random.com.usstocks;


import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import amber.random.com.usstocks.fragments.TokenDialogFragment;
import amber.random.com.usstocks.fragments.companies.CompaniesFragment;
import amber.random.com.usstocks.fragments.companies_details.CompaniesDetailsFragment;
import amber.random.com.usstocks.injection.App;
import amber.random.com.usstocks.preference.AppPreferences;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements CompaniesFragment.Contract,
        TokenDialogFragment.TokenDialogListener {
    public static final String COMPANIES_TAG = "companies_tag";
    public static final String COMPANY_DETAILS_TAG = "company_details_tag";
    public static final String TOKEN_TAG = "token_dialog_tag";
    @Inject
    protected AppPreferences mAppPreferences;
    private CompaniesFragment mCompaniesFragment = null;
    private CompaniesDetailsFragment mCompaniesDetailsFragment = null;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupStrictMode();
        App.getRequestComponent().inject(this);
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
        else
            verifyLiveToken();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        disposeDisposable();
        super.onSaveInstanceState(outState);
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
    public void showDetails(String filter) {
        mCompaniesDetailsFragment = CompaniesDetailsFragment.newInstance(filter);
        getSupportFragmentManager().beginTransaction().hide(mCompaniesFragment)
                .addToBackStack(null)
                .add(R.id.mainfrag, mCompaniesDetailsFragment, COMPANY_DETAILS_TAG)
                .commit();
    }


    @Override
    public void showTokenDialog(int descResId, TokenDialogFragment.TokenDialogListener listener, boolean showCancelButton) {
        TokenDialogFragment dialogFragment = TokenDialogFragment.newInstance(descResId, showCancelButton);
        if (listener != null)
            dialogFragment.setClickListener(listener);
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

    private void disposeDisposable() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
    }

    private void verifyLiveToken() {
        disposeDisposable();
        mDisposable = this.mAppPreferences.hasToken()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(res ->
                {
                    if (mDisposable.isDisposed())
                        return;

                    if (Boolean.TRUE.equals(res))
                        initializeFragments();
                    else
                        showTokenDialog(R.string.token_dialog_desc, this, false);
                });
    }

}
