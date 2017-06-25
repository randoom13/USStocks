package amber.random.com.usstocks.fragments.companies;

import android.database.Cursor;
import android.view.ViewGroup;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.fragments.base.BaseRecyclerCursorAdapter;
import amber.random.com.usstocks.fragments.base.BaseRecyclerFragment;

public class CompaniesCursorAdapter extends BaseRecyclerCursorAdapter<CompanyHolder> {
    private String mMaxId;

    public CompaniesCursorAdapter(BaseRecyclerFragment activity) {
        super(activity);
        // https://www.neotechsoftware.com/blog/android-intent-size-limit
        mSelectionInfoProxy = new CompaniesSelectionInfoProxy(500, activity.getActivity());
    }

    @Override
    public CompanyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new CompanyHolder(mInflater.inflate(R.layout.company_row, viewGroup, false), this);
    }

    @Override
    public void onBindViewHolder(CompanyHolder holder, int i) {
        mDataCursor.moveToPosition(i);
        holder.bindModel(mDataCursor, mMaxId);
    }

    public void updateCursor(Cursor dataCursor, String maxId) {
        this.mMaxId = maxId;
        super.updateCursor(dataCursor);
    }
}