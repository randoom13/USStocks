package amber.random.com.usstocks.restdata;

import com.google.gson.annotations.SerializedName;

public class Company {
   @SerializedName("company_id")
   public int id;
   @SerializedName("name_latest")
   public String latestName;
   @SerializedName("names_previous")
   public String[] previousNames;
}
