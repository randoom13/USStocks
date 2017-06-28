package amber.random.com.usstocks.fragments.base;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.os.CancellationSignal;

import java.util.Map;

import javax.inject.Inject;

import amber.random.com.usstocks.database.DataBaseHelper;

public abstract class BaseSelectionInfoProxy {
    public static final int CHOICE_MODE_MULTIPLE = 4;
    public static final int CHOICE_MODE_SINGLE = 8;
    private static final String SELECTIONS_INFO = "selectionsinfo";
    private static final String SELECTION_MODE_INFO = "selection_mode_info";
    private final int mMaxCacheSize;
    private final Object mlock = new Object();
    protected String mFilter = "";
    protected Activity mActivity;
    private ParcelableSelectedCache mCheckedCache = new ParcelableSelectedCache();
    private boolean mSyncing = false;
    private Cursor mCursor;
    private CancellationSignal mCancellationSignal = new CancellationSignal();
    private int mMode = CHOICE_MODE_SINGLE;

    public BaseSelectionInfoProxy(int maxCacheSize, Activity activity) {
        mCursor = null;
        mMaxCacheSize = maxCacheSize;
        mActivity = activity;
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        if (mode != CHOICE_MODE_SINGLE && mode != CHOICE_MODE_MULTIPLE)
            throw new IllegalArgumentException();

        this.mMode = mode;
    }

    public void setFilter(String filter, boolean isAsync, SyncCompletedCallback callback) {
        this.mFilter = filter;
        launchDatabaseSync(isAsync, false, callback);
    }

    private void resetSync() {
        mSyncing = false;
    }

    public void setSelection(int position, boolean isSelected) {
        if (mMode == CHOICE_MODE_MULTIPLE)
            setMultipleSelection(position, isSelected);
        else if (mMode == CHOICE_MODE_SINGLE)
            setSingleSelection(position, isSelected);
    }

    private void setMultipleSelection(int position, boolean isSelected) {
        synchronized (mlock) {
            mCursor.moveToPosition(position);
            int companyId = mCursor.getInt(0);
            boolean cursorIsChecked = mCursor.getInt(1) > 0;
            if (isSelected != cursorIsChecked)
                mCheckedCache.put(companyId, isSelected);
            else
                mCheckedCache.remove(companyId);
        }
        if (!mSyncing && mCheckedCache.size() > mMaxCacheSize) {
            launchDatabaseSync(true, false, null);
        }
    }

    private void setSingleSelection(int position, boolean isSelected) {
        synchronized (mlock) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                int companyId = mCursor.getInt(0);
                if (mCursor.getInt(1) > 0) {
                    mCheckedCache.put(companyId, false);
                    if (mMaxCacheSize < mCheckedCache.size() + 1) {
                        mCheckedCache.clear();
                        mCursor.moveToPosition(position);
                        mCheckedCache.put(mCursor.getInt(0), true);
                        launchDatabaseSync(true, true, null);
                        return;
                    }
                } else
                    mCheckedCache.remove(companyId);
            }

            mCursor.moveToPosition(position);
            mCheckedCache.put(mCursor.getInt(0), true);
        }
    }

    public Parcelable getSelectionsInfo() {
        return mCheckedCache;
    }

    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(SELECTIONS_INFO, getSelectionsInfo());
        state.putInt(SELECTION_MODE_INFO, mMode);
    }

    public void onRestoreInstanceState(Bundle state) {
        if (state != null) {
            mCheckedCache = state.getParcelable(SELECTIONS_INFO);
            mMode = state.getInt(SELECTION_MODE_INFO, CHOICE_MODE_SINGLE);
        }
        if (mCheckedCache == null)
            mCheckedCache = new ParcelableSelectedCache();
    }

    public int getSelectedCount() {
        int count = 0;
        synchronized (mlock) {
            if (mCursor == null)
                return 0;

            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                int companyId = mCursor.getInt(0);
                Boolean isChecked = mCheckedCache.get(companyId);
                if (isChecked != null) {
                    if (isChecked)
                        count++;
                } else if (mCursor.getInt(1) > 0)
                    count++;
            }
        }
        return count;
    }

    public void closeResources() {
        if (mCursor != null)
            mCursor.close();
    }

    private void swapCursor(Cursor cursor, Map<Integer, Boolean> syncCheckedCache) {
        synchronized (mlock) {
            mSyncing = false;
            closeResources();
            mCursor = cursor;
            cursor.moveToFirst();
            for (Map.Entry<Integer, Boolean> item : syncCheckedCache.entrySet()) {
                if (mCheckedCache.containsKey(item.getKey()) &&
                        mCheckedCache.containsValue(item.getValue()))
                    mCheckedCache.remove(item.getKey());
            }
        }
    }

    private void launchDatabaseSync(boolean isAsync, boolean resetSelection,
                                    SyncCompletedCallback callback) {
        mCancellationSignal.cancel();
        mCancellationSignal = new CancellationSignal();
        mSyncing = true;
        launchDatabaseSync(isAsync, resetSelection, mCancellationSignal, callback);
    }

    protected abstract void launchDatabaseSync(boolean isAsync,
                                               boolean resetSelection,
                                               CancellationSignal cancellation,
                                               SyncCompletedCallback callback);

    public boolean isSelected(int position) {
        synchronized (mlock) {
            mCursor.moveToPosition(position);
            int companyId = mCursor.getInt(0);
            Boolean isChecked = mCheckedCache.get(companyId);
            if (isChecked != null)
                return isChecked;

            boolean cursorIsChecked = mCursor.getInt(1) == 1;
            return cursorIsChecked;
        }
    }

    public interface SyncCompletedCallback {
        void callBack(boolean isSuccessful);
    }

    public abstract class BaseSyncWithDataBaseRunnable implements Runnable {
        protected final boolean mResetSelection;
        @Inject
        protected DataBaseHelper mDataBaseHelper;
        protected CancellationSignal mCancellation;
        private boolean mIsHandleException;
        private SyncCompletedCallback mCompletedCallback;

        public BaseSyncWithDataBaseRunnable(boolean isHandleException,
                                            CancellationSignal cancellation,
                                            boolean resetSelection) {
            mCancellation = cancellation;
            mIsHandleException = isHandleException;
            mResetSelection = resetSelection;
        }

        public void addCompletedListener(SyncCompletedCallback callback) {
            mCompletedCallback = callback;
        }

        protected abstract Map<Integer, Boolean> getSyncCheckedinfo(DataBaseHelper database,
                                                                    Map<Integer, Boolean> checkedCache);

        @Override
        public void run() {
            try {
                String filter = mFilter;
                Map<Integer, Boolean> syncCheckedCache =
                        getSyncCheckedinfo(mDataBaseHelper,
                                mCheckedCache);

                Cursor cursor = mDataBaseHelper
                        .getCompaniesCheckedState(filter);
                if (mCancellation.isCanceled() || !mFilter.equals(filter))
                    return;

                swapCursor(cursor, syncCheckedCache);
                onCompleted(true);
                resetCompletedCallback();
            } catch (Exception ex) {
                onCompleted(false);
                resetCompletedCallback();
                if (!mCancellation.isCanceled())
                    resetSync();
                else return;

                if (!mIsHandleException)
                    throw ex;
            }
        }

        private void resetCompletedCallback() {
            mCompletedCallback = null;
        }

        private void onCompleted(boolean isSuccessful) {
            if (mCompletedCallback != null)
                mCompletedCallback.callBack(isSuccessful);

        }
    }
}
