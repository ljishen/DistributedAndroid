package org.droidphy.core.network.raft.rpc;

import android.net.Uri;
import com.google.common.util.concurrent.SettableFuture;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import org.droidphy.core.network.raft.JSONStringBody;
import org.droidphy.core.network.raft.Jackson;
import org.droidphy.core.utils.Util;

public abstract class SimpleHttp<T> {
    private Uri uri;
    private Object request;

    private Class<T> typeClazz;

    public SimpleHttp(Uri baseUri, String path, Object request) {
        uri = new Uri.Builder().scheme(baseUri.getScheme())
                .encodedAuthority(baseUri.getAuthority())
                .encodedPath(path).build();

        this.request = request;
        this.typeClazz = Util.resolveGenericTypeClass(getClass());
    }

    public SettableFuture<T> execute() {
        AsyncHttpRequest httpRequestrequest = createHttpRequest(uri);

        String jsonRequest = Jackson.serialize(request);
        if (jsonRequest != null) {
            httpRequestrequest.setBody(new JSONStringBody(jsonRequest));
        }

        final SettableFuture<T> result = SettableFuture.create();
        AsyncHttpClient.getDefaultInstance().executeString(httpRequestrequest,
                new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, String value) {
                if (e != null) {
                    result.setException(e);
                } else {
                    result.set(Jackson.deserialize(value, typeClazz));
                }
            }
        });
        return result;
    }

    protected abstract AsyncHttpRequest createHttpRequest(Uri uri);
}
