package dev.zacsweers.moshix.sealed.sample.java;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonQualifier;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import com.squareup.moshi.Types;

import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Retention;

import dev.zacsweers.moshix.records.RecordsJsonAdapterFactory;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public final class RecordJsonAdapterFactoryTest {

  private final Moshi moshi = new Moshi.Builder()
      .add(new RecordsJsonAdapterFactory())
      .build();

  @Test
  public void genericRecord() throws IOException {
    var adapter = moshi.<GenericRecord<String>>adapter(Types.newParameterizedType(GenericRecord.class, String.class));
    assertThat(adapter.fromJson("{\"value\":\"Okay!\"}"))
        .isEqualTo(new GenericRecord<>("Okay!"));
  }

  @Test
  public void genericBoundedRecord() throws IOException {
    var adapter = moshi.<GenericBoundedRecord<Integer>>adapter(
        Types.newParameterizedType(GenericBoundedRecord.class,
        Integer.class));
    assertThat(adapter.fromJson("{\"value\":4}"))
        .isEqualTo(new GenericBoundedRecord<>(4));
  }

  @Test public void qualifiedValues() throws IOException {
    var adapter = moshi
        .newBuilder()
        .add(new ColorAdapter())
        .build()
        .adapter(QualifiedValues.class);
    assertThat(adapter.fromJson("{\"value\":\"#ff0000\"}")).isEqualTo(new QualifiedValues(16711680));
  }

  @Test public void jsonName() throws IOException {
    var adapter = moshi.adapter(JsonName.class);
    assertThat(adapter.fromJson("{\"actualValue\":3}")).isEqualTo(new JsonName(3));
  }
}

record QualifiedValues(@HexColor int value) {}

@Retention(RUNTIME)
@JsonQualifier
@interface HexColor {
}

/** Converts strings like #ff0000 to the corresponding color ints. */
class ColorAdapter {
  @ToJson
  String toJson(@HexColor int rgb) {
    return String.format("#%06x", rgb);
  }

  @FromJson
  @HexColor int fromJson(String rgb) {
    return Integer.parseInt(rgb.substring(1), 16);
  }
}

record JsonName(@Json(name = "actualValue") int value) {}
