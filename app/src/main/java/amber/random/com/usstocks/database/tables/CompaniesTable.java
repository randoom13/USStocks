package amber.random.com.usstocks.database.tables;

public class CompaniesTable {
    public final String mID = "company_id";
    public final String mLATEST_NAME = "name_latest";
    public final String mName = "companies";
    public final String mPrevious_Names = "names_previous";
    public final String mCreateScript;

    //Unfortunately, REPLACE works wrong in virtual table. It always insert new row.
    //"CREATE VIRTUAL TABLE %s USING fts3("
    public CompaniesTable() {
        mCreateScript = String.format("CREATE TABLE %s("
                + "%s INTEGER NOT NULL PRIMARY KEY," +
                "%s TEXT NOT NULL, %s TEXT)", mName, mID, mLATEST_NAME, mPrevious_Names);
    }
}
