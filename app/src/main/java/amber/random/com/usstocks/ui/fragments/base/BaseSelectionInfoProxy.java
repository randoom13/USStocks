package amber.random.com.usstocks.ui.fragments.base;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

import java.util.Map;

import javax.inject.Inject;

import amber.random.com.usstocks.database.DataBaseHelperProxy;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseSelectionInfoProxy {
    public static final int CHOICE_MODE_MULTIPLE = 4;
    public static final int CHOICE_MODE_SINGLE = 8;
    private static final String sSelectionsInfo = "selectionsinfo";
    private static final String sSelectionModeInfo = "selection_mode_info";
    private final int mMaxCacheSize;
    private final Object mlock = new Object();
    protected String mFilter = "";
    @Inject
    protected DataBaseHelperProxy mDataBaseHelper;
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

    public Observable<Boolean> setFilter(String filter) {
        this.mFilter = filter;
        return launchDatabaseSync(false);
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
            disposeDisposable();
            mDisposable = launchDatabaseSync(false)
                    .subscribeOn(Schedulers.computation())
                    .subscribe();
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
                        disposeDisposable();
                        mDisposable = launchDatabaseSync(true)
                                .subscribeOn(Schedulers.computation())
                                .subscribe();
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
        state.putParcelable(sSelectionsInfo, getSelectionsInfo());
        state.putInt(sSelectionModeInfo, mMode);
    }

    public void onRestoreInstanceState(Bundle state) {
        if (state != null) {
            mCheckedCache = state.getParcelable(sSelectionsInfo);
            mMode = state.getInt(sSelectionModeInfo, CHOICE_MODE_SINGLE);
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
        disposeDisposable();

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

    private Observable<Boolean> launchDatabaseSync(boolean resetSelection) {
        mCancellationSignal.cancel();
        mCancellationSignal = new CancellationSignal();
        mSyncing = true;
        return launchDatabaseSync(resetSelection, mCancellationSignal);
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

    protected Observable<Boolean> launchDatabaseSync(boolean resetSelection,
                                                     CancellationSignal cancellation) {

        String filter = mFilter;
        return Observable.fromCallable(() -> {
            Map<Integer, Boolean> syncCheckedCache =
                    getSyncCheckedInfo(mDataBaseHelper, resetSelection,
                            mCheckedCache, cancellation);

            Cursor cursor = mDataBaseHelper.getCompaniesCheckedState(filter);
            if (cancellation.isCanceled() || !mFilter.equals(filter))
                return false;

            swapCursor(cursor, syncCheckedCache);
            return true;
        })
                .onErrorReturn(ex -> {
                    Log.e(getClass().getSimpleName(), "Failed to sync selected companies for filter =" + filter, ex);
                            if (!cancellation.isCanceled())
                                resetSync();
                            if (!(ex instanceof SQLException))
                                throw (Exception) ex;

                            return false;
                        }
                );
    }


    private void disposeDisposable() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }


    protected abstract Map<Integer, Boolean> getSyncCheckedInfo(DataBaseHelperProxy database,
                                                                boolean resetSelection,
                                                                Map<Integer, Boolean> checkedCache,
                                                                CancellationSignal cancellation);
}
