package amber.random.com.usstocks.fragments.base;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import amber.random.com.usstocks.fragments.base.BaseContractListFragment;

public abstract class CommonMultiSelectableListFragment<T> extends BaseContractListFragment<T>
        implements AdapterView.OnItemLongClickListener, AbsListView.MultiChoiceModeListener {
    private static final String STATE_MODE = "model";
    private static final String STATE_CHOICE_MODE = "choice_Mode";
    protected ActionMode mActiveMode = null;

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (mode != null) {
            mActiveMode = null;
            getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
            getListView().setAdapter(getListView().getAdapter());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnItemLongClickListener(this);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(this);

        int choiceMode = savedInstanceState == null ?
                ListView.CHOICE_MODE_NONE
                : savedInstanceState.getInt(STATE_CHOICE_MODE);
        getListView().setChoiceMode(choiceMode);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        l.setItemChecked(position, true);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mActiveMode = mode;
        updateSubtitle(mode);
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        updateSubtitle(mActiveMode);
        return false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (mode != null)
            updateSubtitle(mode);
    }

    protected abstract void updateSubtitle(ActionMode mode);

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CHOICE_MODE, getListView().getChoiceMode());

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setItemChecked(position, true);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }
}
