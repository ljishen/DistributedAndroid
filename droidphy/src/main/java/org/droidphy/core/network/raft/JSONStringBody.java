package org.droidphy.core.network.raft;

import com.google.common.net.MediaType;
import com.koushikdutta.async.http.body.StringBody;

public class JSONStringBody extends StringBody {

    private String json;

    public JSONStringBody(String json) {
        super(json);
    }

    @Override
    public String getContentType() {
        return MediaType.JSON_UTF_8.toString();
    }
}
