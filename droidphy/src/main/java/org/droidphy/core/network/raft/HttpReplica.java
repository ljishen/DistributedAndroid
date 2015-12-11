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
import org.robotninjas.barge.Replica;

public class HttpReplica implements Replica {

    private final Uri uri;

    public HttpReplica(Uri uri) {
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpReplica)) return false;

        HttpReplica that = (HttpReplica) o;

        return Objects.equal(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    boolean match(Uri uri) {
        return this.uri.equals(uri);
    }

    public Uri getUri() {
        return uri;
    }
}
