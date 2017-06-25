package amber.random.com.usstocks.database.tables;


public class SelectedCompaniesTable {
    public final String mID = "id";
    public final String mName = "selected_companies";
    public final String mCreateScript;

    public SelectedCompaniesTable() {
        mCreateScript = String.format("CREATE TABLE %s("
                + "%s INTEGER NOT NULL PRIMARY KEY UNIQUE)", mName, mID);
    }
}
