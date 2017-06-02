package krasa.formatter.templates;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;

public class LiveTemplatesProvider implements DefaultLiveTemplatesProvider {
  @Override
  public String[] getDefaultLiveTemplateFiles() {
    return new String[]{"/liveTemplates/templates"};
  }

  @Override
  public String[] getHiddenLiveTemplateFiles() {
    return null;
  }
}