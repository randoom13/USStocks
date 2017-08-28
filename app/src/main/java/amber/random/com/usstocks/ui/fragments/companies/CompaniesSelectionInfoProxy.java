package amber.random.com.usstocks.ui.fragments.companies;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

import java.util.HashMap;

import javax.inject.Inject;

import amber.random.com.usstocks.App;
import amber.random.com.usstocks.database.DataBaseHelperProxy;
import amber.random.com.usstocks.ui.fragments.base.ParcelableSelectedCache;
import amber.random.com.usstocks.ui.fragments.base.SelectionInfoProxyCapable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private Cursor mCursor;
    private CancellationSignal mCancellationSignal = new CancellationSignal();
    private int mSelectionMode = CHOICE_MODE_SINGLE;
    private Disposable mDisposable;
    private DatabaseSynchronizable mHandler;
    private boolean mSelectionsInvalidated;

    public CompaniesSelectionInfoProxy(int maxCacheSize) {
        mCursor = null;
        mMaxCacheSize = maxCacheSize;
        App.getRequestComponent().inject(this);
    }

    //region SelectionInfoProxyCapable implementation

    @Override
    public void setSynchronizationHandler(DatabaseSynchronizable handler) {
        mHandler = handler;
    }

    @Override
    public int getSelectionMode() {
        return mSelectionMode;
    }

    @Override
    public void setSelectionMode(int selectionMode) {
        if (selectionMode != CHOICE_MODE_SINGLE && selectionMode != CHOICE_MODE_MULTIPLE)
            throw new IllegalArgumentException("selectionMode");

        this.mSelectionMode = selectionMode;
    }

    @Override
    public Observable<Boolean> setFilter(String filter) {
        this.mFilter = filter;
        return launchDatabaseSync(false);
    }

    @Override
    public boolean setSelection(int position, boolean isSelected) {
        if (mSelectionMode == CHOICE_MODE_MULTIPLE) {
            setMultipleSelection(position, isSelected);
            return isSelected;
        } else if (mSelectionMode == CHOICE_MODE_SINGLE) {
            setSingleSelection(position);
            return true;
        } else {
            mSelectionsInvalidated = false;
            return false;
        }
    }

    @Override
    public boolean isSelectionsInvalidated() {
        return mSelectionsInvalidated;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(sSelectionsInfo, getSelectionsInfo());
        state.putInt(sSelectionModeInfo, mSelectionMode);
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        if (state != null) {
            mSelectedCache = state.getParcelable(sSelectionsInfo);
            mSelectionMode = state.getInt(sSelectionModeInfo, CHOICE_MODE_SINGLE);
        }
        if (mSelectedCache == null)
            mSelectedCache = new ParcelableSelectedCache();
    }

    @Override
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

    @Override
    public void closeResources() {
        disposeDisposable();

        if (mCursor != null)
            mCursor.close();
        mHandler = null;
    }

    @Override
    public boolean isSelected(int position) {
        synchronized (mLock) {
            mCursor.moveToPosition(position);
            return isSelected();
        }
    }

    //endregion SelectionInfoProxyCapable implementation

    private boolean isSelected() {
        int companyId = mCursor.getInt(0);
        Boolean isSelected = mSelectedCache.get(companyId);
        if (isSelected != null)
            return isSelected;
        return mCursor.getInt(1) == 1;
    }

    private void disposeDisposable() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    private void setMultipleSelection(int position, boolean isSelected) {
        synchronized (mLock) {
            mCursor.moveToPosition(position);
            int companyId = mCursor.getInt(0);
            boolean cursorIsSelected = mCursor.getInt(1) > 0;
            if (isSelected != cursorIsSelected) {
                mSelectedCache.put(companyId, isSelected);
            } else {
                mSelectedCache.remove(companyId);
            }
        }
        mSelectionsInvalidated = false;
        if (mSelectedCache.size() >= mMaxCacheSize) {
            internalLauchDatabaseSync();
        }
    }

    private void setSingleSelection(int position) {
        boolean needSync = false;
        synchronized (mLock) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                int companyId = mCursor.getInt(0);
                if (mCursor.getInt(1) > 0) {
                    mSelectedCache.put(companyId, false);
                    if (mMaxCacheSize <= mSelectedCache.size()) {
                        mSelectedCache.clear();
                        needSync = true;
                        break;
                    }
                } else
                    mSelectedCache.remove(companyId);
            }

            mCursor.moveToPosition(position);
            mSelectedCache.put(mCursor.getInt(0), true);
        }

        if (needSync) {
            mSelectionsInvalidated = false;
            internalLauchDatabaseSync();
        } else
            mSelectionsInvalidated = true;
    }

    private void internalLauchDatabaseSync() {
        disposeDisposable();
        mDisposable = launchDatabaseSync(true)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((res) -> {
                    if (Boolean.TRUE.equals(res) && null != mHandler)
                        mHandler.syncCompleted();
                });
    }

    private Parcelable getSelectionsInfo() {
        return mSelectedCache;
    }

    private void swapCursor(Cursor cursor) {
        synchronized (mLock) {
            disposeDisposable();
            Cursor oldCursor = mCursor;
            mCursor = cursor;
            mSelectedCache.clear();
            cursor.moveToFirst();
            if (oldCursor != null)
                oldCursor.close();
        }
    }

    private Observable<Boolean> launchDatabaseSync(boolean resetSelection) {
        HashMap<Integer, Boolean> selectedCacheCopy = new HashMap(mSelectedCache);
        mCancellationSignal.cancel();
        mCancellationSignal = new CancellationSignal();
        return launchDatabaseSync(resetSelection, selectedCacheCopy, mCancellationSignal);
    }

    private Observable<Boolean> launchDatabaseSync(boolean resetSelection, HashMap<Integer, Boolean> selectedCache,
                                                   CancellationSignal cancellation) {

        String filter = mFilter;
        return Observable.fromCallable(() -> {
            if (resetSelection)
                mDataBaseHelper.unSelectCompanies(mFilter);

            mDataBaseHelper.setSelectedCompanies(selectedCache);
            cancellation.throwIfCanceled();
            Cursor cursor = mDataBaseHelper.getCompaniesSelectedState(filter);
            if (cancellation.isCanceled() || !mFilter.equals(filter))
                return false;

            swapCursor(cursor);
            return true;
        })
                .onErrorReturn(ex -> {
                            Log.e(getClass().getSimpleName(), "Failed to sync selected companies for filter = " + filter, ex);

                            if (!(ex instanceof SQLException))
                                throw (Exception) ex;

                            return false;
                        }
                );
    }

}
