package amber.random.com.usstocks.ui.fragments.base;

public interface SelectableAdapter<T> {
    void setSelected(T holder, int position, boolean isSelected);

    boolean isSelected(int position);

    int getSelectedCount();

    boolean isLongClick(T holder, int position);

    void addSelectionChangedListener(listener listener);

    void resetSelectionChangedListener();

    boolean isMultiSelectMode();

    void closeMultiSelectMode();

    interface listener {
        void callback();
    }
}
