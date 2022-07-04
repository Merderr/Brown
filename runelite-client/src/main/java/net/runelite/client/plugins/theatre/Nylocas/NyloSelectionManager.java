package net.runelite.client.plugins.theatre.Nylocas;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import net.runelite.client.ui.overlay.Overlay;

public class NyloSelectionManager extends Overlay {
    private final NyloSelectionBox melee;

    private final NyloSelectionBox mage;

    private final NyloSelectionBox range;

    public NyloSelectionBox getMelee() {
        return this.melee;
    }

    public NyloSelectionBox getMage() {
        return this.mage;
    }

    public NyloSelectionBox getRange() {
        return this.range;
    }

    private boolean isHidden = true;

    public boolean isHidden() {
        return this.isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    private Rectangle meleeBounds = new Rectangle();

    public Rectangle getMeleeBounds() {
        return this.meleeBounds;
    }

    private Rectangle rangeBounds = new Rectangle();

    public Rectangle getRangeBounds() {
        return this.rangeBounds;
    }

    private Rectangle mageBounds = new Rectangle();

    public Rectangle getMageBounds() {
        return this.mageBounds;
    }

    public NyloSelectionManager(NyloSelectionBox melee, NyloSelectionBox mage, NyloSelectionBox range) {
        this.mage = mage;
        this.melee = melee;
        this.range = range;
    }

    public Dimension render(Graphics2D graphics) {
        if (this.isHidden)
            return null;
        Dimension meleeD = this.melee.render(graphics);
        graphics.translate(meleeD.width + 1, 0);
        Dimension rangeD = this.range.render(graphics);
        graphics.translate(rangeD.width + 1, 0);
        Dimension mageD = this.mage.render(graphics);
        graphics.translate(-meleeD.width - rangeD.width - 2, 0);
        this.meleeBounds = new Rectangle(getBounds().getLocation(), meleeD);
        this.rangeBounds = new Rectangle(new Point((getBounds().getLocation()).x + meleeD.width + 1, (getBounds()).y), rangeD);
        this.mageBounds = new Rectangle(new Point((getBounds().getLocation()).x + meleeD.width + 1 + rangeD.width + 1, (getBounds()).y), mageD);
        return new Dimension(meleeD.width + rangeD.width + mageD.width, Math.max(Math.max(meleeD.height, rangeD.height), mageD.height));
    }
}
