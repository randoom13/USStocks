package amber.random.com.usstocks.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amber.random.com.usstocks.database.tables.AllIndicatorsTable;
import amber.random.com.usstocks.database.tables.CompaniesTable;
import amber.random.com.usstocks.database.tables.SelectedCompaniesTable;
import amber.random.com.usstocks.models.Company;
import amber.random.com.usstocks.models.Indicator;

public class DataBaseHelper extends SQLiteOpenHelper {
    //region company table columns
    public static final String sCOMPANY_ID = "company_id";
    public static final String sCOMPANY_NAME = "comany_name";
    public static final String sCOMPANY_PREVIOUS_NAMES = "company_old_names";
    //endregion company table columns

    private static final int SCHEMA = 1;
    private static final String DATABASE_NAME = "us_stocks";
    private static DataBaseHelper sINSTANCE;
    private CompaniesTable mCompaniesTable = new CompaniesTable();
    private AllIndicatorsTable mAllIndicatorsTable = new AllIndicatorsTable();
    private SelectedCompaniesTable mSelectedCompaniesTable = new SelectedCompaniesTable();

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    private static String toString(List<String> items) {
        StringBuilder builder = new StringBuilder();
        if (items != null) {
            boolean hasItems = false;
            for (String item : items) {
                if (hasItems)
                    builder.append("/");
                else hasItems = true;
                builder.append(item);
            }
        }
        return builder.toString();
    }

    private static String toParse(Collection<String> items) {
        StringBuilder builder = new StringBuilder();
        boolean hasItems = false;
        for (String item : items) {
            if (hasItems)
                builder.append(",");
            else
                hasItems = true;
            builder.append(item);
        }
        return builder.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new RuntimeException("How did we get here?");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(mCompaniesTable.mCreateScript);
            db.execSQL(mAllIndicatorsTable.mCreateScript);
            db.execSQL(mSelectedCompaniesTable.mCreateScript);
            db.setTransactionSuccessful();
            Log.e(this.getClass().getSimpleName(), "All tables in database was created successfully");
        } catch (SQLException ex) {
            Log.e(this.getClass().getSimpleName(), "Can't create tables!", ex);
        } finally {
            db.endTransaction();
        }
    }

/*    public Cursor getSelectedCompaniesNames(){
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(mCompaniesTable.mName, new String[]{mCompaniesTable.mName},
                mCompaniesTable.mIsSelected + " = 1", null, null, null , null);
        cursor.moveToFirst();
        return cursor;
    }*/

    public Cursor getSelectedCompanies(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "";
        if (TextUtils.isEmpty(filter)) {
            query = " SELECT * FROM " + mSelectedCompaniesTable.mName;
        } else {
            query = "SELECT " + mCompaniesTable.mID + " FROM "
                    + mCompaniesTable.mName + " WHERE " + mCompaniesTable.mLATEST_NAME
                    + "  LIKE " + DatabaseUtils.sqlEscapeString("%" + filter + "%");

            query = "SELECT " + mSelectedCompaniesTable.mID + " FROM " +
                    mSelectedCompaniesTable.mName + " INNER JOIN (" +
                    query + " ) ON " + mSelectedCompaniesTable.mID
                    + " = " + mCompaniesTable.mID;
        }

        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }


    public Cursor getCompaniesCheckedState(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT " + mCompaniesTable.mID +
                ", (SELECT 1 FROM " + mSelectedCompaniesTable.mName + " WHERE " +
                mSelectedCompaniesTable.mID + " = " + mCompaniesTable.mID + " LIMIT 1) " +
                " FROM " + mCompaniesTable.mName;
        if (!TextUtils.isEmpty(filter)) {
            query += " WHERE " + mCompaniesTable.mLATEST_NAME + " LIKE " +
                    DatabaseUtils.sqlEscapeString("%" + filter + "%");
        }
        query += " ORDER BY " + mCompaniesTable.mID;
        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }

    public Map<Integer, Boolean> getCheckCompaniesById(Map<Integer, Boolean> checkedCache) {
        Map<Integer, Boolean> syncCheckCompanies = new HashMap<Integer, Boolean>();
        if (checkedCache.isEmpty())
            return syncCheckCompanies;
        SQLiteDatabase database = getReadableDatabase();
        StringBuilder unCheckedItemsBuilder = new StringBuilder();
        StringBuilder checkedItemsbuilder = new StringBuilder();
        for (Map.Entry<Integer, Boolean> cache : checkedCache.entrySet()) {
            StringBuilder builder = cache.getValue() ? checkedItemsbuilder : unCheckedItemsBuilder;
            if (builder.length() > 0)
                builder.append(",");
            builder.append(cache.getKey());
        }
        StringBuilder positiveTable = new StringBuilder();
        if (checkedItemsbuilder.length() > 0) {
            positiveTable.append("SELECT " + mSelectedCompaniesTable.mID + ", 1 FROM " +
                    mSelectedCompaniesTable.mName + " WHERE EXISTS ( SELECT ");
            positiveTable.append(checkedItemsbuilder);
            positiveTable.append(") ");
        }
        StringBuilder negativeTable = new StringBuilder();
        if (unCheckedItemsBuilder.length() > 0) {
            negativeTable.append("SELECT " + mSelectedCompaniesTable.mID + ", 0 FROM " +
                    mSelectedCompaniesTable.mName + " WHERE NOT EXISTS( SELECT ");
            negativeTable.append(unCheckedItemsBuilder);
            negativeTable.append(")");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(positiveTable);
        if (positiveTable.length() > 0 && negativeTable.length() > 0)
            builder.append(" UNION ");
        builder.append(negativeTable);
        Cursor cursor = database.rawQuery(builder.toString(), null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            syncCheckCompanies.put(cursor.getInt(0), cursor.getInt(1) == 1);
        }
        return syncCheckCompanies;
    }

    public void unSelectCompanies(String filter) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            boolean hasFilter = !TextUtils.isEmpty(filter);
            String sql = "";
            sql = "DELETE FROM  " + mSelectedCompaniesTable.mName;
            if (hasFilter) {
                sql += " WHERE " + mSelectedCompaniesTable.mID +
                        " IN ( ";
                sql += "SELECT " + mCompaniesTable.mID + " FROM " +
                        mCompaniesTable.mName;
                sql += " WHERE " + mCompaniesTable.mLATEST_NAME + " LIKE "
                        + DatabaseUtils.sqlEscapeString("%" + filter + "%") + " )";
            }
            database.execSQL(sql);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void checkCompaniesById(Map<Integer, Boolean> checkedCache) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            for (Map.Entry<Integer, Boolean> cache :
                    checkedCache.entrySet()) {
                int id = cache.getKey();
                if (cache.getValue()) {
                    ContentValues values = new ContentValues();
                    values.put(mSelectedCompaniesTable.mID, id);
                    database.replace(mSelectedCompaniesTable.mName,
                            null, values);
                } else database.delete(mSelectedCompaniesTable.mName,
                        mSelectedCompaniesTable.mID + "=" + id, null);
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
            for (Indicator indicator : companies) {
                ContentValues values = new ContentValues();
                values.put(mAllIndicatorsTable.mName, toParse(indicator.mNames));
                values.put(mAllIndicatorsTable.mID, indicator.mId);
                database.replaceOrThrow(mAllIndicatorsTable.mName, null, values);
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
            for (Company company : companies) {
                ContentValues values = new ContentValues();
                values.put(mCompaniesTable.mLATEST_NAME, company.latestName());
                values.put(mCompaniesTable.mID, company.id());
                values.put(mCompaniesTable.mPrevious_Names, toString(company.previousNames()));
                database.replace(mCompaniesTable.mName, null, values);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public int getMaxId(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT MAX(" + mCompaniesTable.mID + ") FROM "
                + mCompaniesTable.mName;
        if (!TextUtils.isEmpty(filter)) {
            query += " WHERE " + mCompaniesTable.mLATEST_NAME + " LIKE " +
                    DatabaseUtils.sqlEscapeString("%" + filter + "%");
        }
        Cursor cursor = database.rawQuery(query, null);
        int maxId = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return maxId;
    }

    public Cursor getCompanies(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String latestName = mCompaniesTable.mLATEST_NAME;
        String query = "SELECT rowid _id, " + mCompaniesTable.mID + " " + sCOMPANY_ID + ", " +
                latestName + " " + sCOMPANY_NAME + ", " + mCompaniesTable.mPrevious_Names +
                " " + sCOMPANY_PREVIOUS_NAMES + " FROM " + mCompaniesTable.mName;
        if (!TextUtils.isEmpty(filter)) {
            query += " WHERE " + mCompaniesTable.mLATEST_NAME + " LIKE " +
                    DatabaseUtils.sqlEscapeString("%" + filter + "%");
        }
        query += " ORDER BY " + mCompaniesTable.mID;
        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }
}
