/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.castle.tool;

import io.confluent.castle.common.CastleLog;

import java.io.IOException;
import java.nio.file.Paths;

public class CastleEnvironment {
    public static final String CLUSTER_FILE_NAME = "cluster.conf";
    private final String workingDirectory;

    public CastleEnvironment(String workingDirectory) {
        this.workingDirectory = toAbsolutePath(workingDirectory);
    }

    private String toAbsolutePath(String path) {
        if (path == null) {
            path = "";
        }
        return Paths.get(path).toAbsolutePath().toString();
    }

    public CastleLog createCastleLog(String nodeName) throws IOException {
        return CastleLog.fromFile(workingDirectory, nodeName, true);
    }

    public String workingDirectory() {
        return workingDirectory;
    }

    public String clusterOutputPath() {
        return Paths.get(workingDirectory, CLUSTER_FILE_NAME).toAbsolutePath().toString();
    }
};
