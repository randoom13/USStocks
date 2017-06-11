package amber.random.com.usstocks.fragments.base;

public interface SelectableAdapter {
    void setSelected(int position, boolean isSelected);

    boolean isSelected(int position);

    int getSelectedCount();

    boolean isLongClick(int position);

    void addSelectionChangedListener(listener listener);

    void resetSelectionChangedListener();

    boolean isMultiSelectMode();

    void closeMultiSelectMode();

    interface listener {
        void callback();
    }
}
