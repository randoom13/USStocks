package amber.random.com.usstocks.ui.fragments.companies;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.ui.fragments.base.BaseRecyclerCursorAdapterv2;
import amber.random.com.usstocks.ui.fragments.base.BaseRecyclerFragment;

public class CompaniesCursorAdapter extends BaseRecyclerCursorAdapterv2<CompanyHolder> {
    private String mCompaniesMaxId;

    public CompaniesCursorAdapter(BaseRecyclerFragment activity) {
        super(activity);
        // https://www.neotechsoftware.com/blog/android-intent-size-limit
        mSelectionInfoProxy = new CompaniesSelectionInfoProxy(300);
    }

    @Override
    protected void refreshSelectedItem(CompanyHolder holder, boolean isSelected) {
        holder.setSelection(isSelected);
    }

    @Override
    public CompanyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null == fragment)
            return null;
        LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
        return new CompanyHolder(inflater.inflate(R.layout.company_row, viewGroup, false), this);
    }

    @Override
    public void onBindViewHolder(CompanyHolder holder, int position) {
        mDataCursor.moveToPosition(position);
        holder.bindModel(mDataCursor, mCompaniesMaxId);
        holder.itemView.setTag(holder);
    }

    public void updateCursor(Cursor dataCursor, String companiesMaxId) {
        this.mCompaniesMaxId = companiesMaxId;
        super.updateCursor(dataCursor);
    }
}