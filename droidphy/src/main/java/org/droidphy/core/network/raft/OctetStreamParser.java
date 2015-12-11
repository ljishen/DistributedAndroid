package org.droidphy.core.network.raft;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.TransformFuture;
import com.koushikdutta.async.parser.AsyncParser;
import com.koushikdutta.async.parser.ByteBufferListParser;

import java.lang.reflect.Type;

public class OctetStreamParser implements AsyncParser<byte[]> {
    @Override
    public Future<byte[]> parse(DataEmitter emitter) {
        return new ByteBufferListParser().parse(emitter)
                .then(new TransformFuture<byte[], ByteBufferList>() {
                    @Override
                    protected void transform(ByteBufferList result) throws Exception {
                        setComplete(result.getAllByteArray());
                    }
                });
    }

    @Override
    public void write(DataSink sink, byte[] value, CompletedCallback completed) {
        Util.writeAll(sink, value, completed);
    }

    @Override
    public Type getType() {
        return byte[].class;
    }
}
