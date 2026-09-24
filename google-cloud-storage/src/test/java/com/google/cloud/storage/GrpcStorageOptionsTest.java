/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.storage;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.grpc.ChannelPoolSettings;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.NoCredentials;
import com.google.storage.v2.StorageSettings;
import org.junit.Test;

public final class GrpcStorageOptionsTest {

  private static GrpcStorageOptions.Builder baseBuilder() {
    return GrpcStorageOptions.grpc()
        .setProjectId("test-proj")
        .setCredentials(NoCredentials.getInstance());
  }

  private static InstantiatingGrpcChannelProvider resolveChannelProvider(GrpcStorageOptions options)
      throws Exception {
    StorageSettings settings = options.getStorageSettings();
    TransportChannelProvider provider = settings.getTransportChannelProvider();
    assertThat(provider).isInstanceOf(InstantiatingGrpcChannelProvider.class);
    return (InstantiatingGrpcChannelProvider) provider;
  }

  @Test
  public void setChannelPoolSettings_propagatedToTransportChannelProvider() throws Exception {
    ChannelPoolSettings channelPoolSettings =
        ChannelPoolSettings.builder()
            .setInitialChannelCount(8)
            .setMaxChannelCount(16)
            .setMaxRpcsPerChannel(50)
            .build();

    GrpcStorageOptions options = baseBuilder().setChannelPoolSettings(channelPoolSettings).build();

    assertThat(options.getChannelPoolSettings()).isEqualTo(channelPoolSettings);
    assertThat(resolveChannelProvider(options).getChannelPoolSettings())
        .isEqualTo(channelPoolSettings);
  }

  @Test
  public void channelPoolSettings_unset_usesDefaultChannelPool() throws Exception {
    GrpcStorageOptions options = baseBuilder().build();

    assertThat(options.getChannelPoolSettings()).isNull();
    // the default channel pool configuration must remain in effect
    assertThat(resolveChannelProvider(options).getChannelPoolSettings()).isNotNull();
  }

  @Test
  public void toBuilder_preservesChannelPoolSettings() {
    ChannelPoolSettings channelPoolSettings =
        ChannelPoolSettings.builder().setMaxChannelCount(4).build();

    GrpcStorageOptions base = baseBuilder().setChannelPoolSettings(channelPoolSettings).build();
    GrpcStorageOptions rebuilt = base.toBuilder().build();

    assertThat(rebuilt.getChannelPoolSettings()).isEqualTo(channelPoolSettings);
    assertThat(rebuilt).isEqualTo(base);
    assertThat(rebuilt.hashCode()).isEqualTo(base.hashCode());
  }
}
