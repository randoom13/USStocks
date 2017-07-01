package amber.random.com.usstocks.models;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class Company {
    public static TypeAdapter<Company> typeAdapter(Gson gson) {
        return new AutoValue_Company.GsonTypeAdapter(gson);
    }

    @SerializedName("company_id")
    public abstract Integer id();

    @SerializedName("name_latest")
    public abstract String latestName();

    @Nullable
    @SerializedName("names_previous")
    public abstract List<String> previousNames();
}
