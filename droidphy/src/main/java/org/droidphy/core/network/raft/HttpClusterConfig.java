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

import android.net.Uri;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.robotninjas.barge.ClusterConfig;
import org.robotninjas.barge.Replica;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Objects.toStringHelper;

/**
 * Configures a cluster based on HTTP transport.
 * <p>
 * A cluster configuration contains one <em>local</em> instance of a {@link HttpReplica replica} and zero or more
 * remote replicas. A configuration can be built from {@link HttpReplica} instances or simply string representing
 * Uris.
 * </p>
 */
public class HttpClusterConfig implements ClusterConfig {

    private final HttpReplica local;
    private final HttpReplica[] remotes;

    public HttpClusterConfig(HttpReplica local, HttpReplica... remotes) {
        this.local = local;
        this.remotes = remotes;
    }

    /**
     * @return the list of all replicas this particular config contains, not differentiating between local and remote instances.
     */
    public List<HttpReplica> getCluster() {
        List<HttpReplica> replicas = Lists.newArrayList(local);
        replicas.addAll(Arrays.asList(remotes));

        return replicas;
    }

    /**
     * Builds an HTTP-based cluster configuration from some replicas descriptors.
     *
     * @param local   the local replica: This is the configuration that will be used by local agent to define itself and
     *                start server endpoint.
     * @param remotes known replicas in the cluster.
     * @return a valid configuration.
     */
    public static
    ClusterConfig from(HttpReplica local, HttpReplica... remotes) {
        return new HttpClusterConfig(local, remotes);
    }

    @Override
    public Replica local() {
        return local;
    }

    @Override
    public Iterable<Replica> remote() {
        return Arrays.<Replica>asList(remotes);
    }

    @Override
    public Replica getReplica(String info) {
        Uri uri = Uri.parse(info);

        if (local.match(uri))
            return local;

        return Iterables.find(remote(), match(uri));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(local, remotes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final HttpClusterConfig other = (HttpClusterConfig) obj;
        return Objects.equal(this.local, other.local) && Arrays.equals(this.remotes, other.remotes);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("local", local)
                .add("remotes", Arrays.deepToString(remotes))
                .toString();
    }

    private Predicate<Replica> match(final Uri uri) {
        return new Predicate<Replica>() {
            @Override
            public boolean apply(Replica input) {
                return input != null && ((HttpReplica) input).match(uri);
            }
        };
    }

}
