package amber.random.com.usstocks.fragments.companies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.fragments.base.BaseRecyclerFragment;
import amber.random.com.usstocks.fragments.base.BaseSelectionInfoProxy;
import amber.random.com.usstocks.fragments.base.SelectableAdapter;
import amber.random.com.usstocks.restdata.UpdateDatabaseService;


public class CompaniesListFragment extends
        BaseRecyclerFragment<CompaniesListFragment.Contract>
        implements BaseSelectionInfoProxy.SyncCompletedCallback {
    private static final String TOKEN = "";
    private final static String sSTATE_QUERY = "jk";
    private EditText mFilter;
    private ProgressBar mProgress;
    private CompaniesCursorAdapter mAdapter;
    private Handler mainHandler;
    private BroadcastReceiver mOnUpdateCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(UpdateDatabaseService.EXTRA_DATA_UPDATE).
                    equals(UpdateDatabaseService.COMPANIES_LIST))
                new LoadCompaniesList().start();
        }
    };

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
        mAdapter.launchSelectionDataSync(mFilter.getText().toString(), true, this);
    }

    @Override
    public void callBack(boolean isSuccessful) {
        if (isSuccessful)
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mContract.showDetails(mFilter.getText().toString());
                }
            });
    }

    private void resetMultiSelect() {
        mAdapter.closeMultiSelectMode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                new LoadCompaniesList().start();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mainHandler = new Handler(getActivity().getMainLooper());
        mAdapter = new CompaniesCursorAdapter(this);
        mAdapter.onRestoreInstanceState(savedInstanceState);
        mAdapter.addSelectionChangedListener(
                new SelectableAdapter.listener() {
                    @Override
                    public void callback() {
                        updateMultiSelectTitle();
                        if (!mAdapter.isMultiSelectMode()) {
                            showDetails();
                        }
                    }
                }
        );
        setAdapter(mAdapter);
        if (savedInstanceState == null)
            new LoadCompaniesList(true).start();
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
    public void onDetach() {
        super.onDetach();
        mAdapter.closeResources();
    }

    private void launchService() {
        Intent intent = new Intent(getActivity(), UpdateDatabaseService.class);
        intent.putExtra(UpdateDatabaseService.EXTRA_DATA_UPDATE, UpdateDatabaseService.COMPANIES_LIST);
        intent.putExtra(UpdateDatabaseService.EXTRA_TOKEN, TOKEN);
        getActivity().startService(intent);
    }

    public interface Contract {
        void showDetails(String filter);
    }

    private class LoadCompaniesList extends Thread {
        private boolean mForceUpdateDatabase;

        public LoadCompaniesList() {
            this(false);
        }

        public LoadCompaniesList(boolean forceUpdateDatabase) {
            mForceUpdateDatabase = forceUpdateDatabase;
        }

        private void onFailed() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Failed to load companies",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void run() {
            try {
                DataBaseHelper dataBaseHelper = DataBaseHelper.getInstance(getActivity());
                String filter = mFilter.getText().toString();

                final Integer maxId = dataBaseHelper.getMaxId(filter);
                final Cursor cursor = dataBaseHelper.getCompanies(filter);
                if (cursor == null) {
                    Log.e(CompaniesListFragment.this.getClass().getSimpleName(), "Can't load companies");
                    onFailed();
                }
                // Anyway, operation cursor.getCount() executes on main thread
                // when cursor move to any position and this first launch "eats" resources
                // To avoid slowing the main thread execute this operation on background
                if (cursor.moveToFirst() || !mForceUpdateDatabase) {
                    mAdapter.launchSelectionDataSync(filter, false);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setVisibility(View.GONE);
                            mAdapter.updateCursor(cursor, maxId.toString());
                            updateMultiSelectTitle();
                        }
                    });
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setVisibility(View.VISIBLE);
                            launchService();
                        }
                    });
                }
            } catch (Exception ex) {
                Log.e(CompaniesListFragment.this.getClass().getSimpleName(), "Can't load companies", ex);
                onFailed();
            }
        }
    }
}

