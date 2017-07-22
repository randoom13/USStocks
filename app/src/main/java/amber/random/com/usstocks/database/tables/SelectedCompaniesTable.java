package amber.random.com.usstocks.database.tables;


public class SelectedCompaniesTable {
    public final String id = "id";
    public final String name = "selected_companies";
    public final String createScript;

    public SelectedCompaniesTable() {
        createScript = String.format("CREATE TABLE %s("
                + "%s INTEGER NOT NULL PRIMARY KEY UNIQUE)", name, id);
    }
}
