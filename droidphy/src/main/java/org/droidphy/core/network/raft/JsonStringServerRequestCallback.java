package org.droidphy.core.network.raft;

import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.StringBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.orhanobut.logger.Logger;
import org.droidphy.core.utils.JacksonUtil;
import org.droidphy.core.utils.Util;

public abstract class JsonStringServerRequestCallback<V, T> implements HttpServerRequestCallback {

    private Class<T> typeClazz;

    public JsonStringServerRequestCallback() {
        typeClazz = Util.resolveGenericTypeClass(getClass(), 1);
    }

    @Override
    public final void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        AsyncHttpRequestBody body = request.getBody();
        if (!(body instanceof StringBody)) {
            Logger.w("Request body is not String: " + body);
            return;
        }

        T t = null;
        if (typeClazz != Void.class) {
            t = JacksonUtil.deserialize(((StringBody) body).get(), typeClazz);
        }

        String value = JacksonUtil.serialize(handle(t));
        response.send(StringBody.CONTENT_TYPE, value);
    }

    public abstract V handle(T t);
}
