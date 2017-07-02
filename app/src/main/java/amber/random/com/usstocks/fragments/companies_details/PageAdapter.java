package amber.random.com.usstocks.fragments.companies_details;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import amber.random.com.usstocks.R;

public class PageAdapter extends RecyclerViewPager.Adapter<PageController> {
    private final LayoutInflater mInflater;
    private Cursor mCursor = null;

    public PageAdapter(LayoutInflater inflater, Cursor cursor) {
        super();
        mInflater = inflater;
        mCursor = cursor;
    }

    @Override
    public int getItemCount() {
        if (mCursor == null)
            return 0;
        return mCursor.getCount();
    }

    @Override
    public PageController onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PageController(mInflater.inflate(R.layout.companies_details_fragment, parent, false));
    }

    @Override
    public void onBindViewHolder(PageController holder, int position) {

    }
}
