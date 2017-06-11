package amber.random.com.usstocks.fragments.base;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

public class BaseRecyclerFragment<T> extends Fragment {
    protected RecyclerView mRecyclerView;
    protected T mContract;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initializeContract();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initializeContract();
        }
    }

    private void initializeContract() {
        try {
            mContract = (T) getActivity();
        } catch (ClassCastException ex) {
            throw new IllegalStateException(getActivity().getClass().getSimpleName() +
                    "does not implement contract interface for " + getClass().getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        mContract = null;
        super.onDetach();
    }

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
