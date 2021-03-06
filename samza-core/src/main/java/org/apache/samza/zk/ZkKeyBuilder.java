/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.samza.zk;

import org.apache.samza.SamzaException;
import com.google.common.base.Strings;

/**
 * The following ZK hierarchy is maintained for Standalone jobs:
 * <pre>
 *   - /
 *      |- groupId/
 *          |- JobModelGeneration/
 *              |- jobModelVersion (data contains the version)
 *              |- jobModelUpgradeBarrier/ (contains barrier related data)
 *              |- jobModels/
 *                 |- 1 (contains job model version 1 as data)
 *                 |- 2
 *          |- processors/
 *              |- 00000001
 *              |- 00000002
 *              |- ...
 * </pre>
 * Note: ZK Node levels without an ending forward slash ('/') represent a leaf node and non-leaf node, otherwise.
 *
 * This class provides helper methods to easily generate/parse the path in the ZK hierarchy.
 */
public class ZkKeyBuilder {
  /**
   * Prefix generated to uniquely identify a particular deployment of a job.
   * TODO: For now, it looks like $jobName-$jobId. We need to add a unique deployment/attempt identifier as well.
   */
  private final String pathPrefix;

  static final String PROCESSORS_PATH = "processors";
  static final String JOBMODEL_GENERATION_PATH = "JobModelGeneration";

  public ZkKeyBuilder(String pathPrefix) {
    if (Strings.isNullOrEmpty(pathPrefix)) {
      throw new SamzaException("Zk PathPrefix cannot be null or empty!");
    }
    this.pathPrefix = pathPrefix.trim();
  }

  public String getRootPath() {
    return "/" + pathPrefix;
  }

  public String getProcessorsPath() {
    return String.format("/%s/%s", pathPrefix, PROCESSORS_PATH);
  }

  /**
   * Static method that helps parse the processorId substring from the ZK path
   *
   * Processor ID is prefixed by "processor-" and is an leaf node in ZK tree. Hence, this pattern is used to extract
   * the processorId.
   *
   * @param path Full ZK path of a registered processor
   * @return String representing the processor ID
   */
  public static String parseIdFromPath(String path) {
    if (!Strings.isNullOrEmpty(path))
      return path.substring(path.lastIndexOf("/") + 1);
    return null;
  }

  public String getJobModelVersionPath() {
    return String.format("%s/%s/jobModelVersion", getRootPath(), JOBMODEL_GENERATION_PATH);
  }

  public String getJobModelPathPrefix() {
    return String.format("%s/%s/jobModels", getRootPath(), JOBMODEL_GENERATION_PATH, pathPrefix);
  }

  public String getJobModelPath(String jobModelVersion) {
    return String.format("%s/%s", getJobModelPathPrefix(), jobModelVersion);
  }

  public String getJobModelVersionBarrierPrefix(String barrierId) {
    return String.format("%s/%s/%s/versionBarriers", getRootPath(), JOBMODEL_GENERATION_PATH, barrierId);
  }
}
