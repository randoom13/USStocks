package amber.random.com.usstocks.database.tables;

public class CompaniesTable {
    public final String id = "company_id";
    public final String latestName = "name_latest";
    public final String name = "companies";
    public final String previousNames = "names_previous";
    public final String createScript;

    //Unfortunately, REPLACE works "wrong" in virtual table. It always insert new row.
    //"CREATE VIRTUAL TABLE %s USING fts3("
    public CompaniesTable() {
        createScript = String.format("CREATE TABLE %s("
                + "%s INTEGER NOT NULL PRIMARY KEY," +
                "%s TEXT NOT NULL, %s TEXT)", name, id, latestName, previousNames);
    }
}
