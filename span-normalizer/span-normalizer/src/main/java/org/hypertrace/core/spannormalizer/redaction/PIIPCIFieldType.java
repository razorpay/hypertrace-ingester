package org.hypertrace.core.spannormalizer.redaction;

public enum PIIPCIFieldType {
  PII("PII"),
  PCI("PCI");

  private final String value;

  PIIPCIFieldType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
