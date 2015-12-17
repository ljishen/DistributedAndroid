/**
 * Copyright 2013-2014 David Rusek <dave dot rusek at gmail dot com>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.droidphy.core.network.raft;

import com.google.common.base.Throwables;
import com.google.common.net.MediaType;
import com.google.inject.Singleton;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.orhanobut.logger.Logger;
import org.robotninjas.barge.ClusterConfig;
import org.robotninjas.barge.NotLeaderException;
import org.robotninjas.barge.api.AppendEntries;
import org.robotninjas.barge.api.AppendEntriesResponse;
import org.robotninjas.barge.api.RequestVote;
import org.robotninjas.barge.api.RequestVoteResponse;
import org.robotninjas.barge.state.Raft;

import javax.inject.Inject;

@Singleton
public class ResourceHandler {

    private final Raft raft;
    private final ClusterConfig clusterConfig;
    private AsyncHttpServer server;

    @Inject
    public ResourceHandler(Raft raft,
                           ClusterConfig clusterConfig) {
        this.raft = raft;
        this.clusterConfig = clusterConfig;
    }

    public AsyncHttpServer listen(int port) {
        server = new AsyncHttpServer() {
            @Override
            protected AsyncHttpRequestBody onUnknownBody(Headers headers) {
                String contentType = headers.get("Content-Type");
                if (contentType == null) {
                    return null;
                }

                String[] values = contentType.split(";");
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }
                for (String ct : values) {
                    if (MediaType.OCTET_STREAM.toString().equals(ct)) {
                        return new OctetStreamBody();
                    }
                }
                return null;
            }
        };

        // TODO: Add error handler!!

        server.post("/raft/init",
                new JsonStringServerRequestCallback<Raft.StateType, Void>() {
            @Override
            public Raft.StateType handle(Void aVoid) {
                try {
                    return raft.init().get();
                } catch (Throwable e) {
                    throw Throwables.propagate(e);
                }
            }
        });

        server.get("/raft/config",
                new JsonStringServerRequestCallback<ClusterConfig, Void>() {
            @Override
            public ClusterConfig handle(Void aVoid) {
                return clusterConfig;
            }
        });

        server.get("/raft/state",
                new JsonStringServerRequestCallback<Raft.StateType, Void>() {
            @Override
            public Raft.StateType handle(Void aVoid) {
                return raft.type();
            }
        });

        server.post("/raft/vote",
                new JsonStringServerRequestCallback<RequestVoteResponse, RequestVote>() {
            @Override
            public RequestVoteResponse handle(RequestVote requestVote) {
                return raft.requestVote(requestVote);
            }
        });

        server.post("/raft/entries",
                new JsonStringServerRequestCallback<AppendEntriesResponse, AppendEntries>() {
            @Override
            public AppendEntriesResponse handle(AppendEntries appendEntries) {
                return raft.appendEntries(appendEntries);
            }
        });

        server.post("/raft/commit", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request,
                                  AsyncHttpServerResponse response) {
                AsyncHttpRequestBody body = request.getBody();
                if (!(body instanceof OctetStreamBody)) {
                    Logger.w("Request body is not Octet Stream: " + body);
                    return;
                }

                byte[] operation = ((OctetStreamBody) body).get();
                try {
                    raft.commitOperation(operation).get();

                    // "No Content"
                    response.code(204).end();
                } catch (NotLeaderException e) {
                    // "Found"
                    response.redirect(e.getLeader().toString());
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        });

        server.listen(port);
        return server;
    }

    public void stop() {
        server.stop();
    }
}
