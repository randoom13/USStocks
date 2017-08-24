package amber.random.com.usstocks.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import amber.random.com.usstocks.database.tables.AllIndicatorsTable;
import amber.random.com.usstocks.database.tables.CommonIndicatorsDataTable;
import amber.random.com.usstocks.database.tables.CompaniesTable;
import amber.random.com.usstocks.database.tables.SelectedCompaniesTable;
import amber.random.com.usstocks.models.Company;
import amber.random.com.usstocks.models.Indicator;
import amber.random.com.usstocks.models.IndicatorInfo;

public class DataBaseHelper extends SQLiteOpenHelper implements DataBaseHelperProxy {
    private static final int sSchema = 1;
    private static final String sDatabaseName = "us_stocks";
    private CompaniesTable mCompaniesTable = new CompaniesTable();
    private AllIndicatorsTable mAllIndicatorsTable = new AllIndicatorsTable();
    private SelectedCompaniesTable mSelectedCompaniesTable = new SelectedCompaniesTable();
    private CommonIndicatorsDataTable mIndicatorsDataTable = new CommonIndicatorsDataTable();

    public DataBaseHelper(Context context) {
        super(context, sDatabaseName, null, sSchema);
    }

    private static String toString(List<String> items) {
        if (items != null) {
            String res = TextUtils.join("/", items);
            return res;
        }
        return "";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new RuntimeException("How did we get here?");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(mCompaniesTable.createScript);
            db.execSQL(mAllIndicatorsTable.createScript);
            db.execSQL(mSelectedCompaniesTable.createScript);
            db.execSQL(mIndicatorsDataTable.CreateScript);
            db.setTransactionSuccessful();
            Log.e(this.getClass().getSimpleName(), "All tables in database was created successfully");
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getSelectedCompanies(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "";
        if (TextUtils.isEmpty(filter)) {
            query = " SELECT * FROM " + mSelectedCompaniesTable.name;
        } else {
            query = "SELECT " + mCompaniesTable.id + " FROM "
                    + mCompaniesTable.name + " WHERE " + getFilterSequence(filter);

            query = "SELECT " + mSelectedCompaniesTable.id + " FROM " +
                    mSelectedCompaniesTable.name + " INNER JOIN (" +
                    query + " ) ON cast(" + mSelectedCompaniesTable.id
                    + " AS TEXT) = " + mCompaniesTable.id;
        }

        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }


    public Cursor getCompaniesSelectedState(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT cast(" + mCompaniesTable.id +
                " AS INTEGER ), (SELECT 1 FROM " + mSelectedCompaniesTable.name + " WHERE " +
                mSelectedCompaniesTable.id + " = cast(" + mCompaniesTable.id + " AS INTEGER) LIMIT 1) " +
                " FROM " + mCompaniesTable.name;
        if (!TextUtils.isEmpty(filter)) {
            query += " WHERE " + getFilterSequence(filter);
        }
        query += " ORDER BY cast(" + mCompaniesTable.id + " AS INTEGER)";
        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }

    public void unSelectCompanies(String filter) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            boolean hasFilter = !TextUtils.isEmpty(filter);
            String sql = "";
            sql = "DELETE FROM  " + mSelectedCompaniesTable.name;
            if (hasFilter) {
                sql += " WHERE " + mSelectedCompaniesTable.id +
                        " IN ( ";
                sql += "SELECT cast(" + mCompaniesTable.id + " AS INTEGER) FROM " +
                        mCompaniesTable.name;
                sql += " WHERE " + getFilterSequence(filter) + ")";
            }
            database.execSQL(sql);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void setSelectedCompanies(Map<Integer, Boolean> selectionCache) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            for (Map.Entry<Integer, Boolean> cache : selectionCache.entrySet()) {
                int id = cache.getKey();
                if (cache.getValue()) {
                    ContentValues values = new ContentValues();
                    values.put(mSelectedCompaniesTable.id, id);
                    database.replaceOrThrow(mSelectedCompaniesTable.name,
                            null, values);
                } else database.delete(mSelectedCompaniesTable.name,
                        mSelectedCompaniesTable.id + "=" + id, null);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void addIndicators(Collection<Indicator> companies) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(mAllIndicatorsTable.name, null, null);
            database.delete(mIndicatorsDataTable.name, null, null);
            for (Indicator indicator : companies) {
                ContentValues values;
                values = new ContentValues();
                values.put(mAllIndicatorsTable.total, indicator.Total);
                values.put(mAllIndicatorsTable.id, indicator.id);
                database.insertOrThrow(mAllIndicatorsTable.name, null, values);

                for (IndicatorInfo info : indicator.infos) {
                    values = new ContentValues();
                    values.put(mIndicatorsDataTable.indicatorValue, info.value);
                    values.put(mIndicatorsDataTable.indicatorYear, info.year);
                    values.put(mIndicatorsDataTable.id, indicator.id);
                    database.insertOrThrow(mIndicatorsDataTable.name, null, values);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void addCompanies(Collection<Company> companies) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(mCompaniesTable.name, null, null);
            for (Company company : companies) {
                ContentValues values = new ContentValues();
                values.put(mCompaniesTable.latestName, company.latestName());
                values.put(mCompaniesTable.id, company.id());
                values.put(mCompaniesTable.previousNames, toString(company.previousNames()));
                database.replace(mCompaniesTable.name, null, values);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public int getCompaniesMaxId(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT MAX( cast(" + mCompaniesTable.id + " AS INTEGER) ) FROM "
                + mCompaniesTable.name;
        if (!TextUtils.isEmpty(filter)) {
            query += " WHERE " + getFilterSequence(filter);
        }
        Cursor cursor = database.rawQuery(query, null);
        int maxId = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return maxId;
    }

    private String getFilterSequence(String filter) {
        if (!TextUtils.isEmpty(filter)) {
            return mCompaniesTable.name + " match " + DatabaseUtils.sqlEscapeString(filter + "*");
        }
        return "";
    }

    public Cursor getCompanies(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "";
        if (!TextUtils.isEmpty(filter)) {
            query = "SELECT rowid _id, snippet(" + mCompaniesTable.name + ",'<b>','</b>','<b>...</b>',0) " + COMPANY_ID +
                    ", snippet(" + mCompaniesTable.name + ",'<b>','</b>','<b>...</b>',1) " + COMPANY_NAME + ", " +
                    mCompaniesTable.previousNames +
                    " " + COMPANY_PREVIOUS_NAMES + " FROM " + mCompaniesTable.name + " WHERE " + getFilterSequence(filter);
        } else query = "SELECT rowid _id, " + mCompaniesTable.id + " " + COMPANY_ID + ", " +
                mCompaniesTable.latestName + " " + COMPANY_NAME + ", " + mCompaniesTable.previousNames +
                " " + COMPANY_PREVIOUS_NAMES + " FROM " + mCompaniesTable.name;
        query += " ORDER BY cast( " + mCompaniesTable.id + " AS INTEGER)";
        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }
}
