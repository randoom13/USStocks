package amber.random.com.usstocks.fragments.companies_details;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.fragments.base.BaseCursorPagerFragment;


public class CompaniesDetailsPagerFragment extends BaseCursorPagerFragment {
    private Integer mCompanyId;

    public CompaniesDetailsPagerFragment(FragmentManager fm, Cursor cursor) {
        super(fm, cursor);
    }

    public CompaniesDetailsPagerFragment(Fragment fm, String filter) {
        super(fm.getChildFragmentManager(), null);
        new ThreadLoadCursor(fm.getActivity(), filter).start();
    }

    @Override
    public Fragment newFragment(Cursor cursor) {
        mCompanyId = cursor.getInt(0);
        return CompaniesDetailsFragment.newInstance(mCompanyId);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mCursor == null)
            return "";
        mCursor.moveToPosition(position);
        Integer in = mCursor.getInt(0);
        return in.toString();
    }

    private class ThreadLoadCursor extends Thread {
        private Activity mActivity;
        private String mFilter;

        public ThreadLoadCursor(Activity activity, String filter) {
            mActivity = activity;
            mFilter = filter;
        }

        @Override
        public void run() {
            DataBaseHelper dataBaseHelper = DataBaseHelper.getInstance(mActivity);
            final Cursor cursor = dataBaseHelper.getSelectedCompanies(mFilter);
            cursor.moveToFirst();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swapCursor(cursor);
                }
            });
        }
    }
}
