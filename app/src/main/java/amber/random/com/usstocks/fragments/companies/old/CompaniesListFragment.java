package amber.random.com.usstocks.fragments.companies.old;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.fragments.base.CommonMultiSelectableListFragment;
import amber.random.com.usstocks.restdata.UpdateDatabaseService;


public class CompaniesListFragment extends
        CommonMultiSelectableListFragment<CompaniesListFragment.Contract> {
    private static final String TOKEN = "36h9GzLD1Vz3avW2Mibvmg";
    private final static String sSTATE_QUERY = "jk";
    private EditText mFilter;
    private ProgressBar mProgress;
    private CompaniesCursorAdapter mAdapter;
    private Handler mainHandler;
    private BroadcastReceiver mOnUpdateCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //this calls go from service i.e. from non-main thread
            if (intent.getStringExtra(UpdateDatabaseService.EXTRA_DATA_UPDATE).
                    equals(UpdateDatabaseService.COMPANIES_LIST))
                new LoadCompaniesList(false).run();
        }
    };

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String html) {
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    @Override
    protected void updateSubtitle(ActionMode mode) {
        mode.setTitle("(" + getListView().getCheckedItemCount() + ")");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(sSTATE_QUERY, mFilter.getText());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.companies_menu, menu);
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        super.onActionItemClicked(mode, item);
        switch (item.getItemId()) {
            case R.id.cancel:
                mActiveMode.finish();
                return true;
        }

        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.multiselect_companies_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.update) {
            mProgress.setVisibility(View.VISIBLE);
            launchService();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.companies_fragment, container, false);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mFilter = (EditText) view.findViewById(R.id.filter);
        mFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new LoadCompaniesList(false).start();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mainHandler = new Handler(getActivity().getMainLooper());
        mAdapter = new CompaniesCursorAdapter(null);
        setListAdapter(mAdapter);
        if (savedInstanceState == null)
            new LoadCompaniesList(true).start();
        else mFilter.setText(savedInstanceState.getCharSequence(sSTATE_QUERY));

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mActiveMode == null) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            int[] selectedCompanyName = new int[1];
            selectedCompanyName[0] = mAdapter.getCompanyID(cursor);
            mContract.showDetailsFor(selectedCompanyName);
        }
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
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null)
            cursor.close();

        super.onDetach();
    }

    private void launchService() {
        Intent intent = new Intent(getActivity(), UpdateDatabaseService.class);
        intent.putExtra(UpdateDatabaseService.EXTRA_DATA_UPDATE, UpdateDatabaseService.COMPANIES_LIST);
        intent.putExtra(UpdateDatabaseService.EXTRA_TOKEN, TOKEN);
        getActivity().startService(intent);
    }

    public interface Contract {
        void showDetailsFor(int[] companies_names);
    }

    private class CompaniesCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;
        private String mMaxIdFormat;

        public CompaniesCursorAdapter(Cursor c) {
            super(getActivity(), c, 0);
            mInflater = LayoutInflater.from(getActivity());
        }

        private int getCompanyID(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndex(DataBaseHelper.sCOMPANY_ID));
        }

        public void setMaxIdLenght(Integer maxId) {
            this.mMaxIdFormat = "%" + maxId.toString().length() + "s";
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            CompanyHolder holder = (CompanyHolder) view.getTag();
            if (holder == null) {
                holder = new CompanyHolder(view);
                view.setTag(holder);
            }


            String id = cursor.getString(cursor.getColumnIndex(DataBaseHelper.sCOMPANY_ID));
            holder.mCompanyId.setText(String.format(mMaxIdFormat, id));
            String name = cursor.getString(cursor.getColumnIndex(DataBaseHelper.sCOMPANY_NAME));
            holder.mCompanyName.setText(name);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.company_row, parent, false);
            return view;
        }
    }

    public class LoadCompaniesList extends Thread {
        private boolean mForceUpdateDatabase;

        public LoadCompaniesList(boolean forceUpdateDatabase) {
            mForceUpdateDatabase = forceUpdateDatabase;
        }


        @Override
        public void run() {
            try {
                DataBaseHelper dataBaseHelper = DataBaseHelper.getInstance(getActivity());
                String filter = mFilter.getText().toString();
                final int maxId = dataBaseHelper.getMaxId(filter);
                final Cursor cursor = dataBaseHelper.getCompanies(filter);
                if (!mForceUpdateDatabase || cursor.getCount() != 0) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setVisibility(View.GONE);
                            mAdapter.setMaxIdLenght(maxId);
                            mAdapter.swapCursor(cursor);
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
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Failed to load companies",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
