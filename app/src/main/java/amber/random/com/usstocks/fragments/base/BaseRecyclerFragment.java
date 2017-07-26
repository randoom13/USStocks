package amber.random.com.usstocks.fragments.base;

import android.support.v7.widget.RecyclerView;

public class BaseRecyclerFragment<T> extends BaseContractFragment<T> {
    protected RecyclerView mRecyclerView;

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRecyclerView.setLayoutManager(layoutManager);
    }

    public RecyclerView getRecyclerView(){
        return mRecyclerView;
    }
}
