package org.hypertrace.core.spannormalizer.redaction;

import static org.hypertrace.core.spannormalizer.constants.SpanNormalizerConstants.REDACTED_FIELD_PREFIX;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.hypertrace.core.datamodel.AttributeValue;

public class PIIPCIField {

  private String name;
  private Set<String> tagKeySet;
  private RegexInfo regexInfo;
  private AttributeValue replacementValue;
  private PIIPCIFieldType piiPciFieldType;

  public PIIPCIField(String name, String regex, List<String> tagKeyList, String type) {
    this.name = name;
    this.tagKeySet = new HashSet<>(tagKeyList);
    if (!StringUtils.isEmpty(regex)) {
      this.regexInfo = new RegexInfo(regex);
    }
    this.replacementValue =
        AttributeValue.newBuilder().setValue(REDACTED_FIELD_PREFIX + name).build();
    this.piiPciFieldType = PIIPCIFieldType.valueOf(type);
  }

  public Set<String> getTagKeySet() {
    return tagKeySet;
  }

  public String getName() {
    return name;
  }

  public AttributeValue getReplacementValue() {
    return replacementValue;
  }

  public Optional<RegexInfo> getRegexInfo() {
    return Optional.ofNullable(regexInfo);
  }

  public PIIPCIFieldType getPiiPciFieldType() {
    return piiPciFieldType;
  }
}
