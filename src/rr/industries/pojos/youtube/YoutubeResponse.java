
package rr.industries.pojos.youtube;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class YoutubeResponse {

    @SerializedName("items")
    @Expose
    public List<ItemsResponse> items = new ArrayList<ItemsResponse>();

}
