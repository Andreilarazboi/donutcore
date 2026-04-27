package ro.andreilarazboi.donutcore.crates.model;

import java.util.ArrayList;
import java.util.List;

public class CrateHologramSettings {

    private boolean enabled = true;
    private List<String> lines = new ArrayList<>();
    private int backgroundColorR = 0;
    private int backgroundColorG = 0;
    private int backgroundColorB = 0;
    private int backgroundAlpha = 0;
    private boolean textShadow = false;
    private int updateInterval = 40;
    private String templateName = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<String> getLines() { return lines; }
    public void setLines(List<String> lines) { this.lines = lines; }

    public int getBackgroundColorR() { return backgroundColorR; }
    public void setBackgroundColorR(int backgroundColorR) { this.backgroundColorR = backgroundColorR; }

    public int getBackgroundColorG() { return backgroundColorG; }
    public void setBackgroundColorG(int backgroundColorG) { this.backgroundColorG = backgroundColorG; }

    public int getBackgroundColorB() { return backgroundColorB; }
    public void setBackgroundColorB(int backgroundColorB) { this.backgroundColorB = backgroundColorB; }

    public int getBackgroundAlpha() { return backgroundAlpha; }
    public void setBackgroundAlpha(int backgroundAlpha) { this.backgroundAlpha = backgroundAlpha; }

    public boolean isTextShadow() { return textShadow; }
    public void setTextShadow(boolean textShadow) { this.textShadow = textShadow; }

    public int getUpdateInterval() { return updateInterval; }
    public void setUpdateInterval(int updateInterval) { this.updateInterval = updateInterval; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
}
