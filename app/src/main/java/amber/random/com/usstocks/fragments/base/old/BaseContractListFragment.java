package amber.random.com.usstocks.fragments.base.old;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.ListFragment;

public class BaseContractListFragment<T> extends ListFragment {

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

}
