package amber.random.com.usstocks.ui.fragments.base;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import io.reactivex.Observable;

import static amber.random.com.usstocks.ui.fragments.base.SelectionInfoProxyCapable.CHOICE_MODE_MULTIPLE;
import static amber.random.com.usstocks.ui.fragments.base.SelectionInfoProxyCapable.CHOICE_MODE_SINGLE;

public abstract class BaseRecyclerCursorAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> implements SelectableAdapter<T>, SelectionInfoProxyCapable.DatabaseSynchronizable {
    protected final WeakReference<BaseRecyclerFragment> mRecyclerFragmentWR;
    @Inject
    protected SelectionInfoProxyCapable mSelectionInfoProxy;
    protected Cursor mDataCursor;
    private listener mSelectionChangedListener;

    public BaseRecyclerCursorAdapter(BaseRecyclerFragment recyclerFragment) {
        mRecyclerFragmentWR = new WeakReference<BaseRecyclerFragment>(recyclerFragment);
    }

    public abstract T onCreateViewHolder(ViewGroup viewGroup, int i);

    public abstract void onBindViewHolder(T holder, int i);

    protected abstract void refreshSelectedItem(T holder, boolean isChecked);

    @Override
    public void syncCompleted() {
        updateVisibleItems();
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null != fragment)
            fragment.getActivity().invalidateOptionsMenu();
    }

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
        return mSelectionInfoProxy.getSelectionMode() == CHOICE_MODE_MULTIPLE;
    }

    @Override
    public void singleSelectionMode() {
        mSelectionInfoProxy.setSelectionMode(CHOICE_MODE_SINGLE);
        updateVisibleItems();
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null != fragment)
            fragment.getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean isLongClick(T holder) {
        mSelectionInfoProxy.setSelectionMode(CHOICE_MODE_MULTIPLE);
        updateVisibleItems();
        setSelected(holder, !mSelectionInfoProxy.isSelected(holder.getAdapterPosition()));
        return true;
    }

    @Override
    public void multiSelectMode() {
        mSelectionInfoProxy.setSelectionMode(CHOICE_MODE_MULTIPLE);
        updateVisibleItems();
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null != fragment)
            mRecyclerFragmentWR.get().getActivity().invalidateOptionsMenu();
    }

    @Override
    public void setSelected(T holder, boolean isSelected) {
        boolean result = mSelectionInfoProxy.setSelection(holder.getAdapterPosition(), isSelected);
        if (mSelectionInfoProxy.isSelectionsInvalidated())
            updateVisibleItems();
        else
            refreshSelectedItem(holder, result);
        if (mSelectionChangedListener != null)
            mSelectionChangedListener.callback();
    }

    private void updateVisibleItems() {
        BaseRecyclerFragment fragment = mRecyclerFragmentWR.get();
        if (null == fragment)
            return;
        LinearLayoutManager layoutManager = (LinearLayoutManager) fragment.getLayoutManager();

        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();


        for (int index = Math.min(firstVisibleItem, lastVisibleItem);
             index <= Math.max(firstVisibleItem, lastVisibleItem); index++) {
            updateVisibleItem(index);
        }
    }

    protected void updateVisibleItem(int index) {
        T holder = getHolder(index);
        if (null != holder) {
            boolean isSelected = mSelectionInfoProxy.isSelected(index);
            refreshSelectedItem(holder, isSelected);
        }
    }

    protected T getHolder(int position) {
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
        notifyDataSetChanged();
        if (oldCursor != null)
            oldCursor.close();
    }
}
