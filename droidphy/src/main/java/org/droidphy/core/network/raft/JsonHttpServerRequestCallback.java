package org.droidphy.core.network.raft;

import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.orhanobut.logger.Logger;
import org.droidphy.core.utils.Util;

public abstract class JsonHttpServerRequestCallback<T> implements HttpServerRequestCallback {

    private Class<T> typeClazz;

    public JsonHttpServerRequestCallback() {
        typeClazz = Util.resolveGenericTypeClass(getClass());
    }

    @Override
    public final void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        AsyncHttpRequestBody body = request.getBody();
        if (!(body instanceof JSONObjectBody)) {
            Logger.w("Request body is not JSON: " + body);
            return;
        }

        T t = null;
        if (!Void.class.equals(typeClazz)) {
            t = Jackson.deserialize(((JSONObjectBody) body).get().toString(), typeClazz);
            if (t == null) {
                return;
            }
        }

        String value = Jackson.serialize(handle(t));
        if (value != null) {
            response.send("application/json; charset=utf-8", value);
        }
    }

    public abstract Object handle(T t);
}
