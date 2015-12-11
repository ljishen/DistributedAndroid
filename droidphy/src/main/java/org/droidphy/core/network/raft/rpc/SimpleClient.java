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
package org.droidphy.core.network.raft.rpc;

import android.net.Uri;
import com.google.common.util.concurrent.ListenableFuture;
import org.robotninjas.barge.api.AppendEntries;
import org.robotninjas.barge.api.AppendEntriesResponse;
import org.robotninjas.barge.api.RequestVote;
import org.robotninjas.barge.api.RequestVoteResponse;
import org.robotninjas.barge.rpc.RaftClient;

public class SimpleClient implements RaftClient {

    private final Uri baseUri;

    public SimpleClient(Uri baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public ListenableFuture<RequestVoteResponse> requestVote(RequestVote request) {
        return new SimpleHttpPost<RequestVoteResponse>(baseUri, "/raft/vote", request) {}.execute();
    }

    @Override
    public ListenableFuture<AppendEntriesResponse> appendEntries(AppendEntries request) {
        return new SimpleHttpPost<AppendEntriesResponse>(baseUri, "/raft/entries", request) {}.execute();
    }
}
