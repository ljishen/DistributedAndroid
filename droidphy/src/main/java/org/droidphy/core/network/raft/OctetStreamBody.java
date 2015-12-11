package org.droidphy.core.network.raft;

import com.google.common.net.MediaType;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;

public class OctetStreamBody implements AsyncHttpRequestBody<byte[]> {
    private byte[] mBodyBytes;

    @Override
    public void write(AsyncHttpRequest request, DataSink sink, CompletedCallback completed) {
        Util.writeAll(sink, mBodyBytes, completed);
    }

    @Override
    public void parse(DataEmitter emitter, final CompletedCallback completed) {
        new OctetStreamParser().parse(emitter).setCallback(new FutureCallback<byte[]>() {
            @Override
            public void onCompleted(Exception e, byte[] result) {
                mBodyBytes = result;
                completed.onCompleted(e);
            }
        });
    }

    @Override
    public String getContentType() {
        return MediaType.OCTET_STREAM.toString();
    }

    @Override
    public boolean readFullyOnRequest() {
        return true;
    }

    @Override
    public int length() {
        return mBodyBytes.length;
    }

    @Override
    public byte[] get() {
        return mBodyBytes;
    }
}
