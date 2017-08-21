package amber.random.com.usstocks.fragments.base;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;

public abstract class BaseRecyclerCursorAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> implements SelectableAdapter {
    protected final WeakReference<BaseRecyclerFragment> mRecyclerFragmentWR;
    protected BaseSelectionInfoProxy mSelectionInfoProxy;
    protected Cursor mDataCursor;
    private listener mSelectionChangedListener;
    private int mMaxVisibleIndex;
    private int mMinVisibleIndex;

    public BaseRecyclerCursorAdapter(BaseRecyclerFragment recyclerFragment) {
        mRecyclerFragmentWR = new WeakReference<BaseRecyclerFragment>(recyclerFragment);
        invalidateVisibleIndices();
    }

    public abstract T onCreateViewHolder(ViewGroup viewGroup, int i);

    public abstract void onBindViewHolder(T holder, int i);

    public abstract void refreshSelection(T holder, boolean isSelected);

    @Override
    public int getItemCount() {
        if (mDataCursor == null)
            return 0;
        return mDataCursor.getCount();
    }

    @Override
    public void onViewDetachedFromWindow(T holder) {
        super.onViewDetachedFromWindow(holder);
        int position = holder.getAdapterPosition();
        if (mMaxVisibleIndex == position)
            mMaxVisibleIndex--;
        if (mMinVisibleIndex == position)
            mMinVisibleIndex++;
    }

    @Override
    public void onViewAttachedToWindow(T holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        mMaxVisibleIndex = Math.max(position, mMaxVisibleIndex);
        mMinVisibleIndex = Math.min(position, mMinVisibleIndex);
        boolean isSelected = mSelectionInfoProxy.isSelected(position);
        refreshSelection(holder, isSelected);
    }

    public Observable<Boolean> launchSelectionDataSync(String filter) {
        return mSelectionInfoProxy.setFilter(filter);
    }

    //region SelectableAdapter implementation
    @Override
    public void resetSelectionChangedListener() {
        mSelectionChangedListener = null;
    }

    @Override
    public void addSelectionChangedListener(listener runnable) {
        mSelectionChangedListener = runnable;
    }

    @Override
    public boolean isMultiSelectMode() {
        return mSelectionInfoProxy.getMode() == BaseSelectionInfoProxy.CHOICE_MODE_MULTIPLE;
    }

    @Override
    public void closeMultiSelectMode() {
        mSelectionInfoProxy.setMode(BaseSelectionInfoProxy.CHOICE_MODE_SINGLE);
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null != fragment)
            fragment.getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean isLongClick(int position) {
        mSelectionInfoProxy.setMode(BaseSelectionInfoProxy.CHOICE_MODE_MULTIPLE);
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null != fragment)
            mRecyclerFragmentWR.get().getActivity().invalidateOptionsMenu();
        setSelected(position, true);
        return true;
    }

    @Override
    public void setSelected(int position, boolean isSelected) {
        mSelectionInfoProxy.setSelection(position, isSelected);
        if (!isMultiSelectMode())
            updateVisibleItemsSelection();
        if (mSelectionChangedListener != null)
            mSelectionChangedListener.callback();
    }

    private void invalidateVisibleIndices() {
        mMaxVisibleIndex = Integer.MIN_VALUE;
        mMinVisibleIndex = Integer.MAX_VALUE;
    }

    private void updateVisibleItemsSelection() {
        for (int index = mMinVisibleIndex; index <= mMaxVisibleIndex; index++) {
            BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
            if (null != fragment) {
                RecyclerView recyclerView = fragment.getRecyclerView();
                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(index));
                if (null != holder) {
                    boolean isSelected = mSelectionInfoProxy.isSelected(index);
                    refreshSelection((T) holder, isSelected);
                }
            }
        }
    }

    @Override
    public int getSelectedCount() {
        return mSelectionInfoProxy.getSelectedCount();
    }

    @Override
    public boolean isSelected(int position) {
        return mSelectionInfoProxy.isSelected(position);
    }

    //endregion SelectableAdapter

    public void onSaveInstanceState(Bundle state) {
        mSelectionInfoProxy.onSaveInstanceState(state);
    }

    public void onRestoreInstanceState(Bundle state) {
        mSelectionInfoProxy.onRestoreInstanceState(state);
    }

    public void closeResources() {
        if (mDataCursor != null)
            mDataCursor.close();

        mSelectionInfoProxy.closeResources();
    }

    public void updateCursor(Cursor dataCursor) {
        Cursor oldCursor = mDataCursor;
        mDataCursor = dataCursor;
        invalidateVisibleIndices();
        this.notifyDataSetChanged();
        if (oldCursor != null)
            oldCursor.close();
    }
}
