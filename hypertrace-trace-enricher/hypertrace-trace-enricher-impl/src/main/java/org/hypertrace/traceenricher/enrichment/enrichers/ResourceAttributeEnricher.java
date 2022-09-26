package org.hypertrace.traceenricher.enrichment.enrichers;

import static org.hypertrace.traceenricher.util.EnricherUtil.getResourceAttribute;

import com.typesafe.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hypertrace.core.datamodel.AttributeValue;
import org.hypertrace.core.datamodel.Event;
import org.hypertrace.core.datamodel.StructuredTrace;
import org.hypertrace.traceenricher.enrichment.AbstractTraceEnricher;
import org.hypertrace.traceenricher.enrichment.clients.ClientRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceAttributeEnricher extends AbstractTraceEnricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceAttributeEnricher.class);
  private static final String RESOURCE_ATTRIBUTES_CONFIG_KEY = "attributes";
  private static final String NODE_SELECTOR_KEY = "node.selector";
  private List<String> resourceAttributesToAdd = new ArrayList<>();

  public List<String> getResourceAttributesToAdd() {
    return resourceAttributesToAdd;
  }

  @Override
  public void init(Config enricherConfig, ClientRegistry clientRegistry) {
    if (enricherConfig.hasPath(RESOURCE_ATTRIBUTES_CONFIG_KEY)) {
      resourceAttributesToAdd = enricherConfig.getStringList(RESOURCE_ATTRIBUTES_CONFIG_KEY);
    }
  }

  private boolean isValidEvent(Event event) {
    if (resourceAttributesToAdd.isEmpty()) {
      return false;
    }
    if (event.getResourceIndex() < 0) {
      return false;
    }
    return (event.getAttributes() != null) && (event.getAttributes().getAttributeMap() != null);
  }

  @Override
  public void enrichEvent(StructuredTrace trace, Event event) {
    try {
      if (!isValidEvent(event)) {
        return;
      }
      Map<String, AttributeValue> attributeMap = event.getAttributes().getAttributeMap();
      for (String resourceAttributeKey : resourceAttributesToAdd) {
        Optional<AttributeValue> resourceAttributeMaybe =
            getResourceAttribute(trace, event, resourceAttributeKey);
        resourceAttributeMaybe.ifPresent(
            attributeValue -> {
              if (NODE_SELECTOR_KEY.equals(resourceAttributeKey)) {
                attributeValue.setValue(
                    attributeValue
                        .getValue()
                        .substring(attributeValue.getValue().lastIndexOf('/') + 1));
              }
              attributeMap.putIfAbsent(resourceAttributeKey, attributeValue);
            });
      }
    } catch (Exception e) {
      LOGGER.error(
          "Exception while enriching event with resource attributes having event id: {}",
          event.getEventId(),
          e);
    }
  }
}
