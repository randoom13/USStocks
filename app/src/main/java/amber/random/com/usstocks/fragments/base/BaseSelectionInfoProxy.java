package amber.random.com.usstocks.fragments.base;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.os.CancellationSignal;

import java.util.Map;

import javax.inject.Inject;

import amber.random.com.usstocks.database.DataBaseHelper;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseSelectionInfoProxy {
    public static final int CHOICE_MODE_MULTIPLE = 4;
    public static final int CHOICE_MODE_SINGLE = 8;
    private static final String SELECTIONS_INFO = "selectionsinfo";
    private static final String SELECTION_MODE_INFO = "selection_mode_info";
    private final int mMaxCacheSize;
    private final Object mlock = new Object();
    protected String mFilter = "";
    @Inject
    protected DataBaseHelper mDataBaseHelper;
    private ParcelableSelectedCache mCheckedCache = new ParcelableSelectedCache();
    private boolean mSyncing = false;
    private Cursor mCursor;
    private CancellationSignal mCancellationSignal = new CancellationSignal();
    private int mMode = CHOICE_MODE_SINGLE;
    private Disposable mDisposable;

    public BaseSelectionInfoProxy(int maxCacheSize) {
        mCursor = null;
        mMaxCacheSize = maxCacheSize;
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
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

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

    protected void launchDatabaseSync(boolean isAsync, boolean resetSelection,
                                      CancellationSignal cancellation,
                                      BaseSelectionInfoProxy.SyncCompletedCallback callback) {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

        mDisposable = io.reactivex.Observable.empty()
                .subscribeOn(isAsync ? Schedulers.computation() : Schedulers.trampoline())
                .subscribe((ex) -> {
                            String filter = mFilter;
                            Map<Integer, Boolean> syncCheckedCache =
                                    getSyncCheckedInfo(mDataBaseHelper, resetSelection,
                                            mCheckedCache, cancellation);

                            Cursor cursor = mDataBaseHelper
                                    .getCompaniesCheckedState(filter);
                            if (cancellation.isCanceled() || !mFilter.equals(filter))
                                return;

                            swapCursor(cursor, syncCheckedCache);
                            onCompleted(true, callback);
                        }, er -> {
                            onCompleted(false, callback);
                            if (!cancellation.isCanceled())
                                resetSync();
                            else return;

                            if (!isAsync && (er instanceof Exception)) {
                                throw ((Exception) er);
                            }
                        }
                );
    }

    private void onCompleted(boolean isSuccessful, SyncCompletedCallback callback) {
        if (callback != null)
            callback.callBack(isSuccessful);
    }

    protected abstract Map<Integer, Boolean> getSyncCheckedInfo(DataBaseHelper database,
                                                                boolean resetSelection,
                                                                Map<Integer, Boolean> checkedCache,
                                                                CancellationSignal cancellation);

    public interface SyncCompletedCallback {
        void callBack(boolean isSuccessful);
    }
}
