package amber.random.com.usstocks.ui.fragments.base;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;

import static amber.random.com.usstocks.ui.fragments.base.SelectionInfoProxyCapable.CHOICE_MODE_MULTIPLE;
import static amber.random.com.usstocks.ui.fragments.base.SelectionInfoProxyCapable.CHOICE_MODE_SINGLE;

public abstract class BaseRecyclerCursorAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> implements SelectableAdapter<T> {
    protected final WeakReference<BaseRecyclerFragment> mRecyclerFragmentWR;
    protected SelectionInfoProxyCapable mSelectionInfoProxy;
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

    protected abstract void refreshSelectedItem(T holder, boolean isChecked);


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
        refreshSelectedItem(holder, isSelected);
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
        return mSelectionInfoProxy.getMode() == CHOICE_MODE_MULTIPLE;
    }

    @Override
    public void closeMultiSelectMode() {
        mSelectionInfoProxy.setMode(CHOICE_MODE_SINGLE);
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null != fragment)
            fragment.getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean isLongClick(T holder, int position) {
        mSelectionInfoProxy.setMode(CHOICE_MODE_MULTIPLE);
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        setSelected(holder, position, !mSelectionInfoProxy.isSelected(position));
        if (null != fragment)
            mRecyclerFragmentWR.get().getActivity().invalidateOptionsMenu();
        return true;
    }

    @Override
    public void setSelected(T holder, int position, boolean isSelected) {
        mSelectionInfoProxy.setSelection(position, isSelected);
        if (!isMultiSelectMode())
            updateVisibleItemsSelection();
        else {
            refreshSelectedItem(holder, isSelected);
        }
        if (mSelectionChangedListener != null)
            mSelectionChangedListener.callback();
    }

    private void invalidateVisibleIndices() {
        mMaxVisibleIndex = Integer.MIN_VALUE;
        mMinVisibleIndex = Integer.MAX_VALUE;
    }

    private void updateVisibleItemsSelection() {
        for (int index = mMinVisibleIndex; index <= mMaxVisibleIndex; index++) {
            T holder = getHolder(index);
            if (null != holder) {
                boolean isSelected = mSelectionInfoProxy.isSelected(index);
                refreshSelectedItem((T) holder, isSelected);
            }
        }
    }


    private T getHolder(int position) {
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null == fragment)
            return null;
        View view = fragment.getRecyclerView().getChildAt(position);
        if (null == view)
            return null;

        T holder = (T) view.getTag();
        return holder;
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
        notifyDataSetChanged();
        if (oldCursor != null)
            oldCursor.close();
    }
}
