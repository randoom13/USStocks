package amber.random.com.usstocks.ui.fragments.companies_details;


import android.database.Cursor;

import com.lsjwzh.widget.recyclerviewpager.TabLayoutSupport;

public class TabAdapter implements TabLayoutSupport.ViewPagerTabLayoutAdapter {

    private final Cursor mCursor;

    public TabAdapter(Cursor cursor) {
        super();
        mCursor = cursor;
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
