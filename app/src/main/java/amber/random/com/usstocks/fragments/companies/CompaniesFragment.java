package amber.random.com.usstocks.fragments.companies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
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

import javax.inject.Inject;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.database.DataBaseHelperProxy;
import amber.random.com.usstocks.exceptions.UpdateFailed;
import amber.random.com.usstocks.fragments.TokenDialogFragment;
import amber.random.com.usstocks.fragments.base.BaseRecyclerFragment;
import amber.random.com.usstocks.injection.App;
import amber.random.com.usstocks.service.UpdateDatabaseService;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class CompaniesFragment extends
        BaseRecyclerFragment<CompaniesFragment.Contract>
        implements TokenDialogFragment.TokenDialogListener {

    private final static String sSTATE_QUERY = "jk";
    @Inject
    protected DataBaseHelperProxy mDataBaseHelper;
    private EditText mFilter;
    private ProgressBar mProgress;
    private CompaniesCursorAdapter mAdapter;
    private Disposable mDisposable;
    private Disposable mTokenDisposable;
    private BroadcastReceiver mOnUpdateCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getStringExtra(UpdateDatabaseService.EXTRA_DATA_UPDATE).
                    equals(UpdateDatabaseService.COMPANIES_LIST))
                return;

            UpdateFailed updateFailed = (UpdateFailed) intent.getSerializableExtra(UpdateDatabaseService.EXTRA_DATA_ERROR);
            if (updateFailed == null) {
                loadCompaniesList(false);
                return;
            }

            mProgress.setVisibility(View.GONE);
            if (updateFailed.invalidToken()) {
                mProgress.setVisibility(View.VISIBLE);
                mContract.showTokenDialog(getString(R.string.invalid_token), CompaniesFragment.this);
                return;
            }

            Snackbar snackbar = Snackbar.make(getRecyclerView(), "Failed to update companies!", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    };

    private void disposeDisposable() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
    }

    private void disposeTokenDisposable() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
    }

    @Override
    public void onClick(boolean isClose) {
        if (isClose)
            getActivity().finish();

        verifyLiveToken();
    }

    private void verifyLiveToken() {
        disposeTokenDisposable();
        mTokenDisposable = mContract.hasToken().subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    if (res) {
                        mProgress.setVisibility(View.VISIBLE);
                        launchService();
                    } else
                        mContract.showTokenDialog(getString(R.string.invalid_token), this);
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(sSTATE_QUERY, mFilter.getText());
        mAdapter.onSaveInstanceState(outState);
    }

    private void updateMultiSelectTitle() {
        if (mAdapter.isMultiSelectMode()) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle(String.format("MultiSelect (%d/%d)",
                    mAdapter.getSelectedCount(), mAdapter.getItemCount()));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (!mAdapter.isMultiSelectMode()) {
            inflater.inflate(R.menu.companies_menu, menu);
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            inflater.inflate(R.menu.multiselect_companies_menu, menu);
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
                .onErrorReturn((ex) -> false)
                .subscribe(res -> {
                    if (Boolean.TRUE.equals(res))
                        mContract.showDetails(filter);
                });


    }

    private void resetMultiSelect() {
        mAdapter.closeMultiSelectMode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        App.getRequestComponent().inject(this);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.companies_fragment_v2, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
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
        if (savedInstanceState == null)
            loadCompaniesList(true);
        else mFilter.setText(savedInstanceState.getCharSequence(sSTATE_QUERY));

        updateMultiSelectTitle();

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
        disposeTokenDisposable();
        mAdapter.closeResources();
        super.onDestroy();
    }

    private void launchService() {
        Intent intent = new Intent(getActivity(), UpdateDatabaseService.class);
        intent.putExtra(UpdateDatabaseService.EXTRA_DATA_UPDATE, UpdateDatabaseService.COMPANIES_LIST);
        intent.putExtra(UpdateDatabaseService.EXTRA_TOKEN, mContract.getTokenKey());
        getActivity().startService(intent);
    }


    private void failedLoadCompanies() {
        mProgress.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar.make(getRecyclerView(),
                "Failed to load companies", Snackbar.LENGTH_LONG);
        snackbar.show();
    }


    private void loadCompaniesList(boolean forceUpdateDatabase) {
        disposeDisposable();
        mDisposable = Observable.fromCallable(() ->
        {
            Log.d(" loadCompaniesList_1", Thread.currentThread().getName());
            String filter = mFilter.getText().toString();
            Integer maxId = mDataBaseHelper.getMaxId(filter);
            Cursor cursor = mDataBaseHelper.getCompanies(filter);
            LoadCompaniesResult result = new LoadCompaniesResult();
            if (cursor == null) {
                Log.e(this.getClass().getSimpleName(), "Can't load companies");
                result.isResult = false;
                return result;
            }
            // Anyway, operation cursor.getCount() executes on main thread
            // when cursor move to any position and this first launch "eats" resources
            // To avoid slowing the main thread execute this operation on background
            if (cursor.moveToFirst() || !forceUpdateDatabase) {
                mAdapter.launchSelectionDataSync(filter).blockingSubscribe();
                result.cursor = cursor;
                result.maxId = maxId;
                return result;
            } else {
                result.isResult = true;
                return result;
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    Log.d(" loadCompaniesList_2", Thread.currentThread().getName());
                    if (Boolean.TRUE.equals(res.isResult)) {
                        mProgress.setVisibility(View.VISIBLE);
                        launchService();
                        return;
                    }

                    if (Boolean.FALSE.equals(res.isResult)) {
                        mProgress.setVisibility(View.GONE);
                        failedLoadCompanies();
                        return;
                    }

                    mProgress.setVisibility(View.GONE);
                    mAdapter.updateCursor(res.cursor, res.maxId.toString());
                    updateMultiSelectTitle();
                }, er -> {
                    Log.d(" loadCompaniesList_3", Thread.currentThread().getName());
                    Log.e(this.getClass().getSimpleName(), "Can't load companies", er);
                    mProgress.setVisibility(View.GONE);
                    failedLoadCompanies();
                });
    }

    public interface Contract {

        void showDetails(String filter);

        void showTokenDialog(String desc, TokenDialogFragment.TokenDialogListener listener);

        String getTokenKey();

        Observable<Boolean> hasToken();
    }

    private class LoadCompaniesResult {
        Cursor cursor;
        Integer maxId;
        Boolean isResult;
    }

}

