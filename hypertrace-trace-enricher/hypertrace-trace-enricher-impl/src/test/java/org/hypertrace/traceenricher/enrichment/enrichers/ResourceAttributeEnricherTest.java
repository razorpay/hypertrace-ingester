package org.hypertrace.traceenricher.enrichment.enrichers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hypertrace.core.datamodel.*;
import org.hypertrace.traceenricher.enrichment.clients.ClientRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResourceAttributeEnricherTest extends AbstractAttributeEnricherTest {

  private final ResourceAttributeEnricher resourceAttributeEnricher =
      new ResourceAttributeEnricher();

  @BeforeAll
  public void setup() {
    String configFilePath =
        Thread.currentThread().getContextClassLoader().getResource("enricher.conf").getPath();
    if (configFilePath == null) {
      throw new RuntimeException("Cannot find enricher config file enricher.conf in the classpath");
    }

    Config fileConfig = ConfigFactory.parseFile(new File(configFilePath));
    Config configs = ConfigFactory.load(fileConfig);
    if (!configs.hasPath("enricher.ResourceAttributeEnricher")) {
      throw new RuntimeException(
          "Cannot find enricher config for ResourceAttributeEnricher in " + configs);
    }
    resourceAttributeEnricher.init(
        configs.getConfig("enricher.ResourceAttributeEnricher"), mock(ClientRegistry.class));
  }

  @Test
  public void noAttributes() {
    Event event = mock(Event.class);
    when(event.getAttributes()).thenReturn(null);
    resourceAttributeEnricher.enrichEvent(null, event);

    when(event.getAttributes()).thenReturn(Attributes.newBuilder().build());
    when(event.getAttributes().getAttributeMap()).thenReturn(null);
    resourceAttributeEnricher.enrichEvent(null, event);
  }

  @Test
  public void noResourceInTrace() {
    // This trace has no resource attributes.
    StructuredTrace trace = getBigTrace();
    for (Event event : trace.getEventList()) {
      int attributeMapSize = 0;
      if (event.getAttributes() != null && event.getAttributes().getAttributeMap() != null) {
        attributeMapSize = event.getAttributes().getAttributeMap().size();
      }
      resourceAttributeEnricher.enrichEvent(trace, event);
      if (event.getAttributes() != null && event.getAttributes().getAttributeMap() != null) {
        assertEquals(attributeMapSize, event.getAttributes().getAttributeMap().size());
      }
    }
  }

  @Test
  public void traceWithResource() {
    StructuredTrace structuredTrace = mock(StructuredTrace.class);
    List<Resource> resourceList = new ArrayList<>();

    resourceList.add(getResource1());
    resourceList.add(getResource2());
    when(structuredTrace.getResourceList()).thenReturn(resourceList);

    Attributes attributes = Attributes.newBuilder().setAttributeMap(new HashMap<>()).build();
    Event event =
        Event.newBuilder()
            .setAttributes(attributes)
            .setEventId(createByteBuffer("event1"))
            .setCustomerId(TENANT_ID)
            .build();
    event.setResourceIndex(0);
    resourceAttributeEnricher.enrichEvent(structuredTrace, event);
    assertEquals(
        resourceAttributeEnricher.getResourceAttributesToAdd().size() - 2,
        event.getAttributes().getAttributeMap().size());
    assertEquals(
        "test-56f5d554c-5swkj",
        event.getAttributes().getAttributeMap().get("host.name").getValue());
    assertEquals(
        "01188498a468b5fef1eb4accd63533297c195a73",
        event.getAttributes().getAttributeMap().get("service.version").getValue());
    assertEquals("10.21.18.171", event.getAttributes().getAttributeMap().get("ip").getValue());

    Event event2 =
        Event.newBuilder()
            .setAttributes(attributes)
            .setEventId(createByteBuffer("event2"))
            .setCustomerId(TENANT_ID)
            .build();
    event2.setResourceIndex(1);
    addAttribute(event2, "service.version", "123");
    addAttribute(event2, "cluster.name", "default");
    resourceAttributeEnricher.enrichEvent(structuredTrace, event2);
    assertEquals(
        resourceAttributeEnricher.getResourceAttributesToAdd().size(),
        event2.getAttributes().getAttributeMap().size());
    assertEquals("123", event2.getAttributes().getAttributeMap().get("service.version").getValue());
    assertEquals(
        "default", event2.getAttributes().getAttributeMap().get("cluster.name").getValue());
    assertEquals(
        "worker-generic", event2.getAttributes().getAttributeMap().get("node.name").getValue());
  }

  private Resource getResource2() {
    Map<String, AttributeValue> resourceAttributeMap =
        new HashMap<>() {
          {
            put(
                "service.version",
                AttributeValue.newBuilder()
                    .setValue("018a468b5fef1eb4accd63533297c195a73")
                    .build());
            put("environment", AttributeValue.newBuilder().setValue("stage").build());
            put(
                "opencensus.exporterversion",
                AttributeValue.newBuilder().setValue("Jaeger-Go-2.23.1").build());
            put("host.name", AttributeValue.newBuilder().setValue("test1-56f5d554c-5swkj").build());
            put("ip", AttributeValue.newBuilder().setValue("10.21.18.1712").build());
            put("client-uuid", AttributeValue.newBuilder().setValue("53a112a715bdf86").build());
            put("node.name", AttributeValue.newBuilder().setValue("worker-generic").build());
            put(
                "cluster.name",
                AttributeValue.newBuilder().setValue("worker-generic-cluster").build());
          }
        };
    return Resource.newBuilder()
        .setAttributes(Attributes.newBuilder().setAttributeMap(resourceAttributeMap).build())
        .build();
  }

  private Resource getResource1() {
    // In ideal scenarios below resource tags are present in spans.
    Map<String, AttributeValue> resourceAttributeMap =
        new HashMap<>() {
          {
            put(
                "service.version",
                AttributeValue.newBuilder()
                    .setValue("01188498a468b5fef1eb4accd63533297c195a73")
                    .build());
            put("environment", AttributeValue.newBuilder().setValue("stage").build());
            put(
                "opencensus.exporterversion",
                AttributeValue.newBuilder().setValue("Jaeger-Go-2.23.1").build());
            put("host.name", AttributeValue.newBuilder().setValue("test-56f5d554c-5swkj").build());
            put("ip", AttributeValue.newBuilder().setValue("10.21.18.171").build());
            put("client-uuid", AttributeValue.newBuilder().setValue("53a112a715bda986").build());
          }
        };
    return Resource.newBuilder()
        .setAttributes(Attributes.newBuilder().setAttributeMap(resourceAttributeMap).build())
        .build();
  }

  private void addAttribute(Event event, String key, String val) {
    event
        .getAttributes()
        .getAttributeMap()
        .put(key, AttributeValue.newBuilder().setValue(val).build());
  }
}