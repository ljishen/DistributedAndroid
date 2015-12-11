package org.droidphy.core.network.raft.rpc;

import android.net.Uri;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpRequest;

public abstract class SimpleHttpGet<T> extends SimpleHttp<T> {

    public SimpleHttpGet(Uri baseUri, String path, Object request) {
        super(baseUri, path, request);
    }

    @Override
    protected AsyncHttpRequest createHttpRequest(Uri uri) {
        return new AsyncHttpGet(uri);
    }
}
