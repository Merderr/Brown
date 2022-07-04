package net.runelite.client.plugins.theatre.Nylocas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;

public class NyloSelectionBox extends Overlay {
    private final InfoBoxComponent component;

    private boolean isSelected = false;

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    private boolean isHovered = false;

    public boolean isHovered() {
        return this.isHovered;
    }

    public void setHovered(boolean isHovered) {
        this.isHovered = isHovered;
    }

    public NyloSelectionBox(InfoBoxComponent component) {
        this.component = component;
    }

    public Dimension render(Graphics2D graphics) {
        if (this.isSelected) {
            this.component.setColor(Color.GREEN);
            this.component.setText("On");
        } else {
            this.component.setColor(Color.RED);
            this.component.setText("Off");
        }
        Dimension result = this.component.render(graphics);
        if (this.isHovered) {
            Color color = graphics.getColor();
            graphics.setColor(new Color(200, 200, 200));
            graphics.drawRect((this.component.getBounds()).x, (this.component.getBounds()).y, (this.component.getBounds()).width, (this.component.getBounds()).height);
            graphics.setColor(color);
        }
        return result;
    }
}
