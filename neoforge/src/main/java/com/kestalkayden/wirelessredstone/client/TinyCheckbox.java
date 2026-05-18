package com.kestalkayden.wirelessredstone.client;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class TinyCheckbox extends AbstractWidget {

    private static final int BORDER_COLOR    = 0xFF373737;
    private static final int UNSELECTED_FILL = 0xFFC6C6C6;
    private static final int SELECTED_FILL   = 0xFF6CC56C;
    private static final int CHECK_COLOR     = 0xFFFFFFFF;

    private static final int BORDER_DISABLED    = 0xFF6B6B6B;
    private static final int UNSELECTED_DISABLED = 0xFF9A9A9A;
    private static final int SELECTED_DISABLED   = 0xFF8AA888;
    private static final int CHECK_DISABLED      = 0xFFBBBBBB;

    private boolean selected;
    private final Consumer<Boolean> onChange;

    public TinyCheckbox(int x, int y, int size, boolean selected, Consumer<Boolean> onChange) {
        super(x, y, size, size, Component.empty());
        this.selected = selected;
        this.onChange = onChange;
    }

    public boolean isSelected() { return selected; }

    public void setSelected(boolean s) { this.selected = s; }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (!active) return;
        selected = !selected;
        onChange.accept(selected);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int x1 = getX();
        int y1 = getY();
        int x2 = x1 + width;
        int y2 = y1 + height;
        g.fill(x1, y1, x2, y2, active ? BORDER_COLOR : BORDER_DISABLED);
        int fill;
        if (selected) fill = active ? SELECTED_FILL : SELECTED_DISABLED;
        else          fill = active ? UNSELECTED_FILL : UNSELECTED_DISABLED;
        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fill);
        if (selected) {
            g.fill(x1 + 3, y1 + 3, x2 - 3, y2 - 3, active ? CHECK_COLOR : CHECK_DISABLED);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
