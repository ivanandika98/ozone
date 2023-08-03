/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.ozone.om.helpers;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hdds.client.ReplicationConfig;

import static org.apache.hadoop.ozone.OzoneConsts.OM_KEY_PREFIX;

/**
 * Information about one initialized upload.
 */
public class OmMultipartUpload {

  private String volumeName;

  private String bucketName;

  private String keyName;

  private String uploadId;

  private Instant creationTime;

  private ReplicationConfig replicationConfig;

  public OmMultipartUpload(String volumeName, String bucketName,
      String keyName, String uploadId) {
    this.volumeName = volumeName;
    this.bucketName = bucketName;
    this.keyName = keyName;
    this.uploadId = uploadId;
  }

  public OmMultipartUpload(String volumeName, String bucketName,
      String keyName, String uploadId, Instant creationDate) {
    this.volumeName = volumeName;
    this.bucketName = bucketName;
    this.keyName = keyName;
    this.uploadId = uploadId;
    this.creationTime = creationDate;
  }

  public OmMultipartUpload(String volumeName, String bucketName,
      String keyName, String uploadId, Instant creationTime,
      ReplicationConfig replicationConfig) {
    this.volumeName = volumeName;
    this.bucketName = bucketName;
    this.keyName = keyName;
    this.uploadId = uploadId;
    this.creationTime = creationTime;
    this.replicationConfig = replicationConfig;
  }

  public static OmMultipartUpload from(String key) {
    String[] split = key.split(OM_KEY_PREFIX);
    if (split.length < 5) {
      throw new IllegalArgumentException("Key " + key
          + " doesn't have enough segments to be a valid multipart upload key");
    }
    String uploadId = split[split.length - 1];
    String volume = split[1];
    String bucket = split[2];
    return new OmMultipartUpload(volume, bucket,
        key.substring(volume.length() + bucket.length() + 3,
            key.length() - uploadId.length() - 1), uploadId);
  }

  /**
   * Get multipart upload ID from the DB key of multipart upload
   * form openKeyTable/openFileTable.
   *
   * The DB keys of openKeyTable and openFileTable are different:
   *   openKeyTable: /{volumeName}/{bucketName}/{keyName}/{uploadId}
   *   openFileTable: /{volumeId}/{bucketId}/{parentId}/{fileName}/{uploadId}
   *
   *
   * Despite the difference, both have the uploadId as the suffix of the DB
   * key, we can extract this suffix to get the upload ID from the DB key.
   *
   * Upload ID format: uploadId = UUIDv4 + "-" + UniqueId#next
   *
   * @param key DB key
   * @return upload ID if uploadId can be extracted from openDBKey
   *         otherwise null
   */
  public static String getUploadIdFromDbKey(String key) {
    String[] split = key.split(OM_KEY_PREFIX);
    if (split.length < 5) {
      return null;
    }

    String uploadId = split[split.length - 1];

    // Similar to the logic of UUID#fromString, but use 6 since there is
    // another "-" between the UUID and the UniqueId
    if (StringUtils.isEmpty(uploadId) ||
        uploadId.split("-").length != 6) {
      return null;
    }

    return uploadId;
  }


  public String getDbKey() {
    return OmMultipartUpload
        .getDbKey(volumeName, bucketName, keyName, uploadId);
  }

  public static String getDbKey(String volume, String bucket, String key,
      String uploadId) {
    return getDbKey(volume, bucket, key) + OM_KEY_PREFIX + uploadId;

  }

  public static String getDbKey(String volume, String bucket, String key) {
    return OM_KEY_PREFIX + volume + OM_KEY_PREFIX + bucket +
        OM_KEY_PREFIX + key;
  }

  public String getVolumeName() {
    return volumeName;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getKeyName() {
    return keyName;
  }

  public String getUploadId() {
    return uploadId;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
  }

  public void setReplicationConfig(ReplicationConfig replicationConfig) {
    this.replicationConfig = replicationConfig;
  }

  public ReplicationConfig getReplicationConfig() {
    return replicationConfig;
  }
}
