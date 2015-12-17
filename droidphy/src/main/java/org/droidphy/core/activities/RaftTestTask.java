package org.droidphy.core.activities;

import android.net.Uri;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFuture;
import com.orhanobut.logger.Logger;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.droidphy.core.network.raft.RaftServer;
import org.droidphy.core.network.raft.rpc.SimpleClient;
import org.droidphy.core.network.raft.rpc.SimpleHttpGet;
import org.droidphy.core.network.raft.rpc.SimpleHttpPost;
import org.droidphy.core.utils.FileUtil;
import org.robotninjas.barge.api.AppendEntries;
import org.robotninjas.barge.api.AppendEntriesResponse;
import org.robotninjas.barge.api.Entry;
import org.robotninjas.barge.state.Raft;
import org.robotninjas.barge.utils.Prober;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@EBean
public class RaftTestTask {
    @Bean
    FileUtil fileUtil;

    private Uri[] uris;

    public RaftTestTask() {
        uris = new Uri[3];
        uris[0] = Uri.parse("http://localhost:56789/");
        uris[1] = Uri.parse("http://localhost:56790/");
        uris[2] = Uri.parse("http://localhost:56791/");
    }

    @Background
    public void run() {
        RaftServer[] servers = new RaftServer[uris.length];
        for (int i = 0; i < servers.length; i++) {
            servers[i] = new RaftServer(i, uris,
                    fileUtil.getWritableDir("log/server_" + i)).start();
        }

        for (Uri uri : uris) {
            new SimpleHttpPost<Raft.StateType>(uri, "/raft/init", "") {
            }.execute();
        }

        new Prober(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isLeader(uris[0]) || isLeader(uris[1]) || isLeader(uris[2]);
            }
        }).probe(10000);

        Uri leaderUri = getLeader();

        System.out.println("Now Leader is: " + leaderUri);

        ListenableFuture<AppendEntriesResponse> future =
                new SimpleClient(uris[0]).appendEntries(AppendEntries.newBuilder()
                        .setLeaderId("foo")
                        .addEntry(Entry.newBuilder()
                                .setCommand("command".getBytes())
                                .setTerm(2).build())
                        .addEntry(Entry.newBuilder()
                                .setCommand("command1".getBytes())
                                .setTerm(2).build())
                        .setCommitIndex(1)
                        .setPrevLogIndex(2)
                        .setPrevLogTerm(3)
                        .setTerm(3)
                        .build());

        try {
            System.out.println(future.get(120, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Logger.e(e, "AppendEntries error");
            throw Throwables.propagate(e);
        } catch (ExecutionException e) {
            Logger.e(e, "AppendEntries error");
            throw Throwables.propagate(e);
        } catch (TimeoutException e) {
            Logger.e(e, "AppendEntries error");
            throw Throwables.propagate(e);
        } finally {
            for (RaftServer server : servers) {
                server.stop();
                server.clean();
            }
        }
    }

    private Uri getLeader() {
        if (isLeader(uris[0]))
            return uris[0];

        if (isLeader(uris[1]))
            return uris[1];

        if (isLeader(uris[2]))
            return uris[2];

        throw new RuntimeException("Expected one server to be a leader");
    }

    private boolean isLeader(Uri uri) {
        try {
            return Raft.StateType.LEADER ==
                    new SimpleHttpGet<Raft.StateType>(uri, "/raft/state", null) {}.execute().get();
        } catch (InterruptedException e) {
            Logger.e(e, "Check raft state interrupted!");
            throw Throwables.propagate(e);
        } catch (ExecutionException e) {
            Logger.e(e, "Execution error while checking raft state");
            throw Throwables.propagate(e);
        }
    }
}
