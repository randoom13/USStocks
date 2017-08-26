package amber.random.com.usstocks.ui.fragments.companies;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

import javax.inject.Inject;

import amber.random.com.usstocks.App;
import amber.random.com.usstocks.database.DataBaseHelperProxy;
import amber.random.com.usstocks.ui.fragments.base.ParcelableSelectedCache;
import amber.random.com.usstocks.ui.fragments.base.SelectionInfoProxyCapable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CompaniesSelectionInfoProxy implements SelectionInfoProxyCapable {
    private static final String sSelectionsInfo = "selectionsinfo";
    private static final String sSelectionModeInfo = "selection_mode_info";
    private final int mMaxCacheSize;
    private final Object mLock = new Object();
    protected String mFilter = "";
    @Inject
    protected DataBaseHelperProxy mDataBaseHelper;
    private ParcelableSelectedCache mSelectedCache = new ParcelableSelectedCache();
    private boolean mSyncing = false;
    private Cursor mCursor;
    private CancellationSignal mCancellationSignal = new CancellationSignal();
    private int mSelectionMode = CHOICE_MODE_SINGLE;
    private Disposable mDisposable;
    private boolean mSelectionInvalidated;

    public CompaniesSelectionInfoProxy(int maxCacheSize) {
        mCursor = null;
        mMaxCacheSize = maxCacheSize;
        App.getRequestComponent().inject(this);
    }

    public int getMode() {
        return mSelectionMode;
    }

    public void setMode(int selectionMode) {
        if (selectionMode != CHOICE_MODE_SINGLE && selectionMode != CHOICE_MODE_MULTIPLE)
            throw new IllegalArgumentException("selectionMode");

        this.mSelectionMode = selectionMode;
    }

    public Observable<Boolean> setFilter(String filter) {
        this.mFilter = filter;
        return launchDatabaseSync(false);
    }

    private void resetSync() {
        mSyncing = false;
    }

    public void setSelection(int position, boolean isSelected) {
        if (mSelectionMode == CHOICE_MODE_MULTIPLE) {
            setMultipleSelection(position, isSelected);
            mSelectionInvalidated = false;
        } else if (mSelectionMode == CHOICE_MODE_SINGLE) {
            setSingleSelection(position);
            mSelectionInvalidated = true;
        } else
            mSelectionInvalidated = false;
    }

    public boolean isSelectionInvalidated() {
        return mSelectionInvalidated;
    }

    private void setMultipleSelection(int position, boolean isSelected) {
        synchronized (mLock) {
            mCursor.moveToPosition(position);
            int companyId = mCursor.getInt(0);
            boolean cursorIsChecked = isSelected();
            if (isSelected != cursorIsChecked)
                mSelectedCache.put(companyId, isSelected);
            else
                mSelectedCache.remove(companyId);
        }
        if (!mSyncing && mSelectedCache.size() > mMaxCacheSize) {
            disposeDisposable();
            mDisposable = launchDatabaseSync(false)
                    .subscribeOn(Schedulers.computation())
                    .subscribe();
        }
    }

    private void setSingleSelection(int position) {
        synchronized (mLock) {
            boolean needSync = false;
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                int companyId = mCursor.getInt(0);
                if (mCursor.getInt(1) > 0) {
                    mSelectedCache.put(companyId, false);
                    if (mMaxCacheSize < mSelectedCache.size() + 1) {
                        mSelectedCache.clear();
                        needSync = true;
                        break;
                    }
                } else
                    mSelectedCache.remove(companyId);
            }

            mCursor.moveToPosition(position);
            mSelectedCache.put(mCursor.getInt(0), true);
            if (needSync) {
                disposeDisposable();
                mDisposable = launchDatabaseSync(true)
                        .subscribeOn(Schedulers.computation())
                        .subscribe();
            }
        }
    }

    public Parcelable getSelectionsInfo() {
        return mSelectedCache;
    }

    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(sSelectionsInfo, getSelectionsInfo());
        state.putInt(sSelectionModeInfo, mSelectionMode);
    }

    public void onRestoreInstanceState(Bundle state) {
        if (state != null) {
            mSelectedCache = state.getParcelable(sSelectionsInfo);
            mSelectionMode = state.getInt(sSelectionModeInfo, CHOICE_MODE_SINGLE);
        }
        if (mSelectedCache == null)
            mSelectedCache = new ParcelableSelectedCache();
    }

    public int getSelectedCount() {
        int count = 0;
        synchronized (mLock) {
            if (mCursor == null)
                return 0;

            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                if (isSelected())
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

    private void swapCursor(Cursor cursor) {
        synchronized (mLock) {
            mSyncing = false;
            closeResources();
            mCursor = cursor;
            cursor.moveToFirst();
            mSelectedCache.clear();
        }
    }

    private Observable<Boolean> launchDatabaseSync(boolean resetSelection) {
        mCancellationSignal.cancel();
        mCancellationSignal = new CancellationSignal();
        mSyncing = true;
        return launchDatabaseSync(resetSelection, mCancellationSignal);
    }

    public boolean isSelected(int position) {
        synchronized (mLock) {
            mCursor.moveToPosition(position);
            return isSelected();
        }
    }

    private boolean isSelected() {
        int companyId = mCursor.getInt(0);
        Boolean isSelected = mSelectedCache.get(companyId);
        if (isSelected != null)
            return isSelected;
        return mCursor.getInt(1) == 1;
    }

    protected Observable<Boolean> launchDatabaseSync(boolean resetSelection,
                                                     CancellationSignal cancellation) {

        String filter = mFilter;
        return Observable.fromCallable(() -> {
            if (resetSelection)
                mDataBaseHelper.unSelectCompanies(mFilter);

            mDataBaseHelper.setSelectedCompanies(mSelectedCache);
            cancellation.throwIfCanceled();
            Cursor cursor = mDataBaseHelper.getCompaniesSelectedState(filter);
            if (cancellation.isCanceled() || !mFilter.equals(filter))
                return false;

            swapCursor(cursor);
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

}
