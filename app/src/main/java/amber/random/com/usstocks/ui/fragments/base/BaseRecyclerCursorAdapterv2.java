package amber.random.com.usstocks.ui.fragments.base;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;

public abstract class BaseRecyclerCursorAdapterv2<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> implements SelectableAdapter {
    protected final WeakReference<BaseRecyclerFragment> mRecyclerFragmentWR;
    protected BaseSelectionInfoProxy mSelectionInfoProxy;
    protected Cursor mDataCursor;
    private listener mSelectionChangedListener;

    public BaseRecyclerCursorAdapterv2(BaseRecyclerFragment recyclerFragment) {
        mRecyclerFragmentWR = new WeakReference<BaseRecyclerFragment>(recyclerFragment);
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
    public void onViewAttachedToWindow(T holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        boolean isChecked = mSelectionInfoProxy.isSelected(position);
        refreshSelectedItem(holder, isChecked);
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


    private void updateVisibleItemsSelection() {
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null == fragment)
            return;
        LinearLayoutManager layoutManager = (LinearLayoutManager) fragment.getLayoutManager();
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

        int minVisibleIndex = Math.min(firstVisibleItem, lastVisibleItem);
        int maxVisibleIndex = Math.max(firstVisibleItem, lastVisibleItem);
        for (int index = minVisibleIndex; index <= maxVisibleIndex; index++) {

            RecyclerView recyclerView = fragment.getRecyclerView();
            View view = recyclerView.getChildAt(index);
            if (null == view) continue;

            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (null == holder) continue;

            boolean isSelected = mSelectionInfoProxy.isSelected(index);
            refreshSelectedItem((T) holder, isSelected);
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
        this.notifyDataSetChanged();
        if (oldCursor != null)
            oldCursor.close();
    }
}
