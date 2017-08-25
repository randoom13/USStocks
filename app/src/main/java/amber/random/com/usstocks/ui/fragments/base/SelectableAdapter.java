package amber.random.com.usstocks.ui.fragments.base;

public interface SelectableAdapter<T> {
    void setSelected(T holder, boolean isSelected);

    boolean isSelected(int position);

    int getSelectedCount();

    boolean isLongClick(T holder);

    void addSelectionChangedListener(listener listener);

    void resetSelectionChangedListener();

    boolean isMultiSelectMode();

    void multiSelectMode();

    void singleSelectionMode();

    interface listener {
        void callback();
    }
}
