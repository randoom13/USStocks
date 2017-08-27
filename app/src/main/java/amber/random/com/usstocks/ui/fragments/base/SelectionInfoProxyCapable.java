package amber.random.com.usstocks.ui.fragments.base;


import android.os.Bundle;

import io.reactivex.Observable;

public interface SelectionInfoProxyCapable {
    int CHOICE_MODE_MULTIPLE = 4;
    int CHOICE_MODE_SINGLE = 8;

    Observable<Boolean> setFilter(String filter);

    void onRestoreInstanceState(Bundle state);

    void onSaveInstanceState(Bundle state);

    boolean isSelected(int position);

    int getSelectionMode();

    void setSelectionMode(int mode);

    int getSelectedCount();

    void closeResources();

    boolean isSelectionsInvalidated();

    void setSelection(int position, boolean isSelected);
}
