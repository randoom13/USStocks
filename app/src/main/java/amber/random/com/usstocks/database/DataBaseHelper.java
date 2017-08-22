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
import java.util.HashMap;
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

    //region company table columns

    public static final String COMPANY_ID = "company_id";
    public static final String COMPANY_NAME = "company_name";
    public static final String COMPANY_PREVIOUS_NAMES = "company_old_names";

    //endregion company table columns

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
                    query + " ) ON " + mSelectedCompaniesTable.id
                    + " = " + mCompaniesTable.id;
        }

        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }


    public Cursor getCompaniesCheckedState(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT " + mCompaniesTable.id +
                ", (SELECT 1 FROM " + mSelectedCompaniesTable.name + " WHERE " +
                mSelectedCompaniesTable.id + " = " + mCompaniesTable.id + " LIMIT 1) " +
                " FROM " + mCompaniesTable.name;
        if (!TextUtils.isEmpty(filter)) {
            query += " WHERE " + getFilterSequence(filter);
        }
        query += " ORDER BY " + mCompaniesTable.id;
        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }

    public Map<Integer, Boolean> getCheckCompaniesById(Map<Integer, Boolean> checkedCache) {
        Map<Integer, Boolean> syncCheckCompanies = new HashMap<Integer, Boolean>();
        if (checkedCache.isEmpty())
            return syncCheckCompanies;

        SQLiteDatabase database = getReadableDatabase();
        StringBuilder unCheckedItemsBuilder = new StringBuilder();
        StringBuilder checkedItemsBuilder = new StringBuilder();

        for (Map.Entry<Integer, Boolean> cache : checkedCache.entrySet()) {
            StringBuilder builder = cache.getValue() ? checkedItemsBuilder : unCheckedItemsBuilder;
            if (builder.length() > 0)
                builder.append(",");
            builder.append(cache.getKey());
        }
        StringBuilder positiveTable = new StringBuilder();
        if (checkedItemsBuilder.length() > 0) {
            positiveTable.append("SELECT " + mSelectedCompaniesTable.id + ", 1 FROM " +
                    mSelectedCompaniesTable.name + " WHERE EXISTS ( SELECT ");
            positiveTable.append(checkedItemsBuilder);
            positiveTable.append(") ");
        }
        StringBuilder negativeTable = new StringBuilder();
        if (unCheckedItemsBuilder.length() > 0) {
            negativeTable.append("SELECT " + mSelectedCompaniesTable.id + ", 0 FROM " +
                    mSelectedCompaniesTable.name + " WHERE NOT EXISTS( SELECT ");
            negativeTable.append(unCheckedItemsBuilder);
            negativeTable.append(")");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(positiveTable);
        if (positiveTable.length() > 0 && negativeTable.length() > 0)
            builder.append(" UNION ");
        builder.append(negativeTable);
        Cursor cursor = database.rawQuery(builder.toString(), null);
        final Integer isChecked = 1;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            syncCheckCompanies.put(cursor.getInt(0), isChecked.equals(cursor.getInt(1)));
        }
        cursor.close();
        return syncCheckCompanies;
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
                sql += "SELECT " + mCompaniesTable.id + " FROM " +
                        mCompaniesTable.name;
                sql += " WHERE " + getFilterSequence(filter);
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
                    values.put(mSelectedCompaniesTable.id, id);
                    database.replace(mSelectedCompaniesTable.name,
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
            for (Indicator indicator : companies) {
                ContentValues values;
                values = new ContentValues();
                values.put(mAllIndicatorsTable.total, indicator.Total);
                values.put(mAllIndicatorsTable.id, indicator.id);
                database.replaceOrThrow(mAllIndicatorsTable.name, null, values);

                for (IndicatorInfo info : indicator.infos) {
                    values = new ContentValues();
                    values.put(mIndicatorsDataTable.indicatorValue, info.value);
                    values.put(mIndicatorsDataTable.indicatorYear, info.year);
                    values.put(mIndicatorsDataTable.id, indicator.id);
                    database.replaceOrThrow(mIndicatorsDataTable.name, null, values);
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

    public int getMaxId(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT MAX(" + mCompaniesTable.id + ") FROM "
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
            StringBuilder builder = new StringBuilder();
            builder.append(mCompaniesTable.latestName);
            builder.append(" LIKE ");
            String formattedFilter = DatabaseUtils.sqlEscapeString("%" + filter + "%");
            builder.append(formattedFilter);
            builder.append(" OR ");
            builder.append(mCompaniesTable.id);
            builder.append(" LIKE ");
            builder.append(formattedFilter);
            return builder.toString();
        }
        return "";
    }

    public Cursor getCompanies(String filter) {
        SQLiteDatabase database = getReadableDatabase();
        String latestName = mCompaniesTable.latestName;
        String query = "SELECT rowid _id, " + mCompaniesTable.id + " " + COMPANY_ID + ", " +
                latestName + " " + COMPANY_NAME + ", " + mCompaniesTable.previousNames +
                " " + COMPANY_PREVIOUS_NAMES + " FROM " + mCompaniesTable.name;
        if (!TextUtils.isEmpty(filter)) {
            query += " WHERE " + getFilterSequence(filter);
        }
        query += " ORDER BY " + mCompaniesTable.id;
        Cursor cursor = database.rawQuery(query, null);
        return cursor;
    }
}
