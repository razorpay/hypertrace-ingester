package org.hypertrace.core.spannormalizer.redaction;

import java.util.regex.Pattern;

public class RegexInfo {
  private String regexString;
  private Pattern regexPattern;

  public RegexInfo(String regexString) {
    this.regexString = regexString;
    this.regexPattern = Pattern.compile(regexString);
  }

  public String getRegexString() {
    return regexString;
  }

  public Pattern getRegexPattern() {
    return regexPattern;
  }

  @Override
  public String toString() {
    return "RegexInfo{"
        + "regexString='"
        + regexString
        + '\''
        + ", regexPattern="
        + regexPattern
        + '}';
  }
}
