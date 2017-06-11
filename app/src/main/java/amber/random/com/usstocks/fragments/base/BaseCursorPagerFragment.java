package amber.random.com.usstocks.fragments.base;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public abstract class BaseCursorPagerFragment extends FragmentStatePagerAdapter {
    protected Cursor mCursor;

    public BaseCursorPagerFragment(FragmentManager fm, Cursor cursor) {
        super(fm);
        mCursor = cursor;
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        notifyDataSetChanged();
        if (oldCursor != null)
            oldCursor.close();
    }

    @Override
    public Fragment getItem(int position) {
        mCursor.moveToPosition(position);
        return newFragment(mCursor);
    }

    public abstract Fragment newFragment(Cursor cursor);

    @Override
    public int getCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

}
