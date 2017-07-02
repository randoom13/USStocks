package amber.random.com.usstocks.fragments.companies_details;


import android.app.Activity;
import android.database.Cursor;

import com.lsjwzh.widget.recyclerviewpager.TabLayoutSupport;

public class TabAdapter implements TabLayoutSupport.ViewPagerTabLayoutAdapter {

    private final Activity mActivity;
    private final Cursor mCursor;

    public TabAdapter(Activity activity, Cursor cursor) {
        super();
        mCursor = cursor;
        mActivity = activity;
    }

    @Override
    public String getPageTitle(int i) {
        mCursor.moveToPosition(i);
        return mCursor.getString(0);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null)
            return 0;
        return mCursor.getCount();
    }
}
