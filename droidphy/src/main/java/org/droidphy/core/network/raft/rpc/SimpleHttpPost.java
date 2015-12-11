package org.droidphy.core.network.raft.rpc;

import android.net.Uri;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;

public abstract class SimpleHttpPost<T> extends SimpleHttp<T> {

    public SimpleHttpPost(Uri baseUri, String path, Object request) {
        super(baseUri, path, request);
    }

    @Override
    protected AsyncHttpRequest createHttpRequest(Uri uri) {
        return new AsyncHttpPost(uri);
    }
}
