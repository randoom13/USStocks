package amber.random.com.usstocks.fragments.base;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;

public abstract class BaseRecyclerCursorAdapter<Holder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<Holder> implements SelectableAdapter {
    protected final WeakReference<BaseRecyclerFragment> mRecyclerFragmentWR;
    protected BaseSelectionInfoProxy mSelectionInfoProxy;
    protected Cursor mDataCursor;
    private listener mSelectionChangedListener;
    private int mMaxVisibleIndex = Integer.MIN_VALUE;
    private int mMinVisibleIndex = Integer.MAX_VALUE;

    public BaseRecyclerCursorAdapter(BaseRecyclerFragment recyclerFragment) {
        mRecyclerFragmentWR = new WeakReference<BaseRecyclerFragment>(recyclerFragment);

    }

    public abstract Holder onCreateViewHolder(ViewGroup viewGroup, int i);

    public abstract void onBindViewHolder(Holder holder, int i);

    @Override
    public int getItemCount() {
        if (mDataCursor == null)
            return 0;
        return mDataCursor.getCount();
    }

    @Override
    public void onViewDetachedFromWindow(Holder holder) {
        super.onViewDetachedFromWindow(holder);
        int position = holder.getAdapterPosition();
        if (mMaxVisibleIndex == position)
            mMaxVisibleIndex--;
        if (mMinVisibleIndex == position)
            mMinVisibleIndex++;
    }

    @Override
    public void onViewAttachedToWindow(Holder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        mMaxVisibleIndex = Math.max(position, mMaxVisibleIndex);
        mMinVisibleIndex = Math.min(position, mMinVisibleIndex);
        boolean isChecked = mSelectionInfoProxy.isSelected(position);
        if (holder.itemView.isActivated() != isChecked)
            holder.itemView.setActivated(isChecked);
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
        if (mRecyclerFragmentWR.isEnqueued())
            mRecyclerFragmentWR.get().getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean isLongClick(int position) {
        mSelectionInfoProxy.setMode(BaseSelectionInfoProxy.CHOICE_MODE_MULTIPLE);
        if (mRecyclerFragmentWR.isEnqueued())
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

    private void updateVisibleItemsSelection() {
        for (int index = mMinVisibleIndex; index <= mMaxVisibleIndex; index++) {
            View view = null;
            if (mRecyclerFragmentWR.isEnqueued())
                view = mRecyclerFragmentWR.get().getRecyclerView().getChildAt(index);
            if (view != null) {
                boolean isChecked = mSelectionInfoProxy.isSelected(index);
                if (view.isActivated() != isChecked)
                    view.setActivated(isChecked);
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
        mMaxVisibleIndex = Integer.MIN_VALUE;
        mMinVisibleIndex = Integer.MAX_VALUE;
        this.notifyDataSetChanged();
        if (oldCursor != null)
            oldCursor.close();
    }
}
