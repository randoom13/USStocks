package amber.random.com.usstocks.ui.fragments.companies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.database.DataBaseHelperProxy;
import amber.random.com.usstocks.exceptions.NoConnectionException;
import amber.random.com.usstocks.exceptions.UpdateFailedException;
import amber.random.com.usstocks.injection.App;
import amber.random.com.usstocks.preference.AppPreferences;
import amber.random.com.usstocks.service.UpdateDatabaseService;
import amber.random.com.usstocks.ui.fragments.TokenDialogFragment;
import amber.random.com.usstocks.ui.fragments.base.BaseRecyclerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class CompaniesFragment extends
        BaseRecyclerFragment<CompaniesFragment.Contract>
        implements TokenDialogFragment.TokenDialogListener {

    private final static String sStateQuery = "jk";
    @Inject
    protected DataBaseHelperProxy mDataBaseHelper;
    @Inject
    protected AppPreferences mAppPreferences;
    private EditText mFilter;

    private TextView mEmptyRecordsList;
    private ProgressBar mProgress;
    private CompaniesCursorAdapter mAdapter;
    private Disposable mDisposable;
    private BroadcastReceiver mOnUpdateCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!UpdateDatabaseService.COMPANIES_LIST.
                    equals(intent.getStringExtra(UpdateDatabaseService.EXTRA_DATA_UPDATE)))
                return;

            UpdateFailedException updateFailed = (UpdateFailedException) intent.getSerializableExtra(UpdateDatabaseService.EXTRA_DATA_ERROR);
            if (updateFailed == null) {
                loadCompaniesList(false);
                return;
            }

            mProgress.setVisibility(View.GONE);
            if (updateFailed.invalidToken()) {
                mContract.showTokenDialog(R.string.invalid_token, CompaniesFragment.this, true);
                return;
            }

            int textResourceId = updateFailed.innerException instanceof NoConnectionException ?
                    R.string.no_connection : R.string.failed_update_companies;
            Snackbar snackbar = Snackbar.make(getRecyclerView(), textResourceId, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    };


    private void disposeDisposable() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
    }

    @Override
    public void onClick(boolean isClose) {
        if (isClose)
            getActivity().finish();
        else
            verifyLiveToken();
    }

    private void verifyLiveToken() {
        disposeDisposable();
        mDisposable = this.mAppPreferences.hasToken()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    if (!mDisposable.isDisposed())
                        if (Boolean.TRUE.equals(res)) {
                            mProgress.setVisibility(View.VISIBLE);
                            launchService();
                        } else
                            mContract.showTokenDialog(R.string.invalid_token, this, true);
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(sStateQuery, mFilter.getText());
        mAdapter.onSaveInstanceState(outState);
    }

    private void updateMultiSelectTitle() {
        if (mAdapter.isMultiSelectMode()) {
            if (null == mToolbar)
                initializeBar(getView());
            mToolbar.setTitle(String.format("MultiSelect (%d/%d)",
                    mAdapter.getSelectedCount(), mAdapter.getItemCount()));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (null == mToolbar)
            initializeBar(getView());
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (!mAdapter.isMultiSelectMode()) {
            mToolbar.inflateMenu(R.menu.companies_menu);
            mToolbar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            mToolbar.inflateMenu(R.menu.multiselect_companies_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        updateMultiSelectTitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                mProgress.setVisibility(View.VISIBLE);
                launchService();
                return true;

            case R.id.show_companies_details:
                showDetails();
                return true;

            case R.id.cancel:
            case android.R.id.home:
                resetMultiSelect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showDetails() {
        disposeDisposable();
        String filter = mFilter.getText().toString();
        mDisposable = mAdapter.launchSelectionDataSync(filter)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    if (Boolean.TRUE.equals(res)) {
                        mContract.showDetails(filter);
                        mToolbar = null;
                    }
                });
    }

    private void resetMultiSelect() {
        mAdapter.closeMultiSelectMode();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        App.getRequestComponent().inject(this);
        initializeBar(view);
        setHasOptionsMenu(true);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mEmptyRecordsList = (TextView) view.findViewById(R.id.empty_records_list);
        mFilter = (EditText) view.findViewById(R.id.filter);
        mFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadCompaniesList(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });
        mFilter.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_black_18dp, 0, 0, 0);
        mAdapter = new CompaniesCursorAdapter(this);
        mAdapter.onRestoreInstanceState(savedInstanceState);
        mAdapter.addSelectionChangedListener(() -> {
                    updateMultiSelectTitle();
                    if (!mAdapter.isMultiSelectMode()) {
                        showDetails();
                    }
                }
        );
        setAdapter(mAdapter);
        if (null == savedInstanceState)
            loadCompaniesList(true);
        else mFilter.setText(savedInstanceState.getCharSequence(sStateQuery));

        updateMultiSelectTitle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.companies_fragment, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(UpdateDatabaseService.UPDATE_COMPLETED);
        LocalBroadcastManager.getInstance(getActivity()).
                registerReceiver(mOnUpdateCompleted, intentFilter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).
                unregisterReceiver(mOnUpdateCompleted);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        disposeDisposable();
        mAdapter.closeResources();
        super.onDestroy();
    }

    private void launchService() {
        mProgress.setVisibility(View.VISIBLE);
        mEmptyRecordsList.setVisibility(View.GONE);
        Intent intent = new Intent(getActivity(), UpdateDatabaseService.class);
        intent.putExtra(UpdateDatabaseService.EXTRA_DATA_UPDATE, UpdateDatabaseService.COMPANIES_LIST);
        getActivity().startService(intent);
    }

    private void failedLoadCompanies() {
        mProgress.setVisibility(View.GONE);
        mEmptyRecordsList.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar.make(getRecyclerView(), R.string.failed_update_companies, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void loadCompaniesList(boolean forceUpdateDatabase) {
        disposeDisposable();
        String filter = mFilter.getText().toString();
        Observable<LoadCompaniesResult> loadCompaniesObservable = Observable.fromCallable(() ->
        {
            Integer maxId = mDataBaseHelper.getCompaniesMaxId(filter);
            Cursor cursor = mDataBaseHelper.getCompanies(filter);
            LoadCompaniesResult result = new LoadCompaniesResult(cursor, maxId, forceUpdateDatabase);
            return result;
        });

        Observable<Boolean> syncSelectionObservable = mAdapter.launchSelectionDataSync(filter);
        mDisposable = Observable.zip(loadCompaniesObservable, syncSelectionObservable,
                (loadCompanies, syncSelection) -> Boolean.TRUE.equals(syncSelection) ? loadCompanies : null)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    if (null == res) {
                        failedLoadCompanies();
                        return;
                    }

                    if (Boolean.TRUE.equals(res.needUpdate)) {
                        launchService();
                        return;
                    }

                    mProgress.setVisibility(View.GONE);
                    mAdapter.updateCursor(res.cursor, res.companiesMaxId.toString());
                    boolean showEmptyRecordsList = res.cursor.getCount() == 0;
                    mEmptyRecordsList.setVisibility(showEmptyRecordsList ? View.VISIBLE : View.GONE);
                    updateMultiSelectTitle();
                }, ex -> {
                    Log.e(this.getClass().getSimpleName(), "Failed to load companies for filter = " + filter, ex);
                    if (!(ex instanceof SQLException))
                        throw new RuntimeException(ex.getMessage());

                    failedLoadCompanies();
                });
    }

    public interface Contract {

        void showDetails(String filter);

        void showTokenDialog(int descResId, TokenDialogFragment.TokenDialogListener listener, boolean showCancelButton);
    }

    private class LoadCompaniesResult {
        public final Cursor cursor;
        public final Integer companiesMaxId;
        public final Boolean needUpdate;

        public LoadCompaniesResult(Cursor cursor, Integer companiesMaxId, boolean forceUpdateDatabase) {
            this.cursor = cursor;
            this.companiesMaxId = companiesMaxId;
            // Anyway, operation cursor.getCount() executes on main thread
            // when cursor move to any position and this first launch "eats" resources
            // To avoid slowing the main thread execute this operation on background
            needUpdate = !(this.cursor.moveToFirst() || !forceUpdateDatabase);
        }
    }

}

