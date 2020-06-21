/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.auto.value.extension.serializable.serializer.impl;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import com.google.auto.value.extension.serializable.serializer.interfaces.Serializer;
import com.google.auto.value.extension.serializable.serializer.utils.CompilationAbstractTest;
import com.google.auto.value.extension.serializable.serializer.utils.FakeSerializerFactory;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import java.util.Optional;
import javax.lang.model.type.TypeMirror;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ImmutableListSerializerExtensionTest extends CompilationAbstractTest {

  private static final String FUNCTION_WITH_EXCEPTIONS =
      "com.google.auto.value.extension.serializable.serializer.runtime.FunctionWithExceptions";
  private static final String IMMUTABLE_LIST = "com.google.common.collect.ImmutableList";

  private ImmutableListSerializerExtension extension;
  private FakeSerializerFactory fakeSerializerFactory;

  @Before
  public void setUpExtension() {
    extension = new ImmutableListSerializerExtension();
    fakeSerializerFactory = new FakeSerializerFactory();
    fakeSerializerFactory.setReturnIdentitySerializer(false);
  }

  @Test
  public void getSerializer_nonImmutableList_emptyReturned() {
    TypeMirror typeMirror = typeMirrorOf(String.class);

    Optional<Serializer> actualSerializer =
        extension.getSerializer(typeMirror, fakeSerializerFactory, mockProcessingEnvironment);

    assertThat(actualSerializer).isEmpty();
  }

  @Test
  public void getSerializer_immutableListWithSerializableContainedType_emptyReturned() {
    fakeSerializerFactory.setReturnIdentitySerializer(true);
    TypeMirror typeMirror = typeMirrorOf(ImmutableList.class);

    Optional<Serializer> actualSerializer =
        extension.getSerializer(typeMirror, fakeSerializerFactory, mockProcessingEnvironment);

    assertThat(actualSerializer).isEmpty();
  }

  @Test
  public void getSerializer_immutableList_serializerReturned() {
    TypeMirror typeMirror = typeMirrorOf(ImmutableList.class);

    Serializer actualSerializer =
        extension.getSerializer(typeMirror, fakeSerializerFactory, mockProcessingEnvironment).get();

    assertThat(actualSerializer.getClass().getName())
        .contains("ImmutableListSerializerExtension$ImmutableListSerializer");
  }

  @Test
  public void proxyFieldType() {
    TypeMirror typeMirror = declaredTypeOf(ImmutableList.class, Integer.class);

    Serializer serializer =
        extension.getSerializer(typeMirror, fakeSerializerFactory, mockProcessingEnvironment).get();
    TypeMirror actualTypeMirror = serializer.proxyFieldType();

    assertThat(typeUtils.isSameType(actualTypeMirror, typeMirror)).isTrue();
  }

  @Test
  public void toProxy() {
    TypeMirror typeMirror = declaredTypeOf(ImmutableList.class, Integer.class);

    Serializer serializer =
        extension.getSerializer(typeMirror, fakeSerializerFactory, mockProcessingEnvironment).get();
    CodeBlock actualCodeBlock = serializer.toProxy(CodeBlock.of("x"));

    assertThat(actualCodeBlock.toString())
        .isEqualTo(
            String.format(
                "x.stream().map(%s.wrapper(value$ -> value$)).collect(%s.toImmutableList())",
                FUNCTION_WITH_EXCEPTIONS, IMMUTABLE_LIST));
  }

  @Test
  public void fromProxy() {
    TypeMirror typeMirror = declaredTypeOf(ImmutableList.class, Integer.class);

    Serializer serializer =
        extension.getSerializer(typeMirror, fakeSerializerFactory, mockProcessingEnvironment).get();
    CodeBlock actualCodeBlock = serializer.fromProxy(CodeBlock.of("x"));

    assertThat(actualCodeBlock.toString())
        .isEqualTo(
            String.format(
                "x.stream().map(%s.wrapper(value$ -> value$)).collect(%s.toImmutableList())",
                FUNCTION_WITH_EXCEPTIONS, IMMUTABLE_LIST));
  }
}
