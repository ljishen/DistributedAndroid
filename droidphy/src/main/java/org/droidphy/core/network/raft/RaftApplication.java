package org.droidphy.core.network.raft;

import android.net.Uri;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.robotninjas.barge.ClusterConfig;
import org.robotninjas.barge.StateMachine;
import org.robotninjas.barge.state.Raft;
import org.robotninjas.barge.state.RaftProtocolListener;
import org.robotninjas.barge.state.StateTransitionListener;
import org.robotninjas.barge.utils.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class RaftApplication {

    private static final Logger logger = LoggerFactory.getLogger(RaftServer.class);

    private final int serverIndex;
    private final Uri[] uris;
    private final File logDir;

    private final List<StateTransitionListener> transitionListeners;
    private final List<RaftProtocolListener> protocolListeners;

    private Optional<Injector> injector = Optional.absent();

    public RaftApplication(
            int serverIndex,
            Uri[] uris,
            File logDir,
            Iterable<StateTransitionListener> transitionListener,
            Iterable<RaftProtocolListener> protocolListener) {
        this.serverIndex = serverIndex;
        this.uris = uris;
        this.logDir = logDir;
        this.transitionListeners = Lists.newArrayList(transitionListener);
        this.protocolListeners = Lists.newArrayList(protocolListener);
    }

    public RaftApplication(int serverIndex, Uri[] uris, File logDir) {
        this(serverIndex, uris, logDir, Collections.<StateTransitionListener>emptyList(), Collections.<RaftProtocolListener>emptyList());
    }

    public void makeResourceConfig() {
        ClusterConfig clusterConfig = HttpClusterConfig.from(
                new HttpReplica(uris[serverIndex]),
                remotes());

        if (!logDir.exists() && !logDir.mkdirs()) {
            logger.warn("failed to create directories for storing logs, bad things will happen");
        }

        StateMachine stateMachine = new StateMachine() {
            int i = 0;

            @Override
            public Object applyOperation(@Nonnull ByteBuffer entry) {
                return i++;
            }
        };

        final RaftModule raftModule = new RaftModule(
                clusterConfig,
                logDir,
                stateMachine,
                1500,
                transitionListeners,
                protocolListeners);

        injector = Optional.of(Guice.createInjector(raftModule));
    }

    private HttpReplica[] remotes() {
        HttpReplica[] remoteReplicas = new HttpReplica[uris.length - 1];

        for (int i = 0; i < remoteReplicas.length; i++) {
            remoteReplicas[i] = new HttpReplica(uris[(serverIndex + i + 1) % uris.length]);
        }

        return remoteReplicas;
    }

    public void clean() throws IOException {
        Files.delete(logDir);
    }

    public void start(int port) {
        injector.get().getInstance(ResourceHandler.class).listen(port);
    }

    public void stop() {
        injector.get().getInstance(ResourceHandler.class).stop();
        injector.transform(new Function<Injector, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Injector input) {
                Raft instance = null;
                if (input != null) {
                    instance = input.getInstance(Raft.class);
                    instance.stop();
                }
                return instance;
            }
        });
    }
}
