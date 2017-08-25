package amber.random.com.usstocks.database.tables;

public class CompaniesTable {
    public final String id = "company_id";
    public final String latestName = "name_latest";
    public final String name = "companies";
    public final String previousNames = "names_previous";
    public final String createScript;

    public CompaniesTable() {
        createScript = String.format("CREATE VIRTUAL TABLE %s USING fts3("
                + "%s , %s, %s)", name, id, latestName, previousNames);
    }
}
