package amber.random.com.usstocks.models;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.List;

@AutoValue
public abstract class Indicator {
    public static TypeAdapter<Indicator> typeAdapter(Gson gson) {
        return new AutoValue_Indicator.GsonTypeAdapter(gson);
    }

    public abstract int mId();

    public abstract List<String> mNames();

}
