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
import com.google.common.base.Throwables;

import java.io.File;
import java.io.IOException;

public class RaftServer {

    private final int serverIndex;
    private final Uri[] uris;
    private final RaftApplication application;

    public RaftServer(int serverIndex, Uri[] uris, File logDir) {
        this.serverIndex = serverIndex;
        this.uris = uris;

        this.application = new RaftApplication(serverIndex, uris, logDir);
    }

    public RaftServer start() {
        application.makeResourceConfig();

        int port = uris[serverIndex].getPort();
        application.start(port == -1 ? 80 : port);
        return this;
    }

    public void stop() {
        application.stop();
    }

    public void clean() {
        try {
            application.clean();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
