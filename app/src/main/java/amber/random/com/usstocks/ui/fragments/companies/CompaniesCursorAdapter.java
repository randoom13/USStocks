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
        holder.setSelected(isSelected);
    }

    @Override
    public void closeMultiSelectMode() {
        super.closeMultiSelectMode();
        updateVisibleItemsSelection();
    }

    @Override
    public boolean isLongClick(CompanyHolder holder) {
        boolean result = super.isLongClick(holder);
        if (result) {
            updateVisibleItemsSelection();
        }
        return result;
    }

    @Override
    public void onViewAttachedToWindow(CompanyHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.setMultiSelectMode(isMultiSelectMode());
    }

    @Override
    public CompanyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null == fragment)
            return null;
        LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
        CompanyHolder holder = new CompanyHolder(inflater.inflate(R.layout.company_row, viewGroup, false), this);
        holder.setMultiSelectMode(isMultiSelectMode());
        return holder;
    }

    protected void updateVisibleItemSelection(int index) {
        CompanyHolder holder = getHolder(index);
        if (null != holder) {
            holder.setMultiSelectMode(isMultiSelectMode());
            boolean isSelected = mSelectionInfoProxy.isSelected(index);
            refreshSelectedItem(holder, isSelected);
        }
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