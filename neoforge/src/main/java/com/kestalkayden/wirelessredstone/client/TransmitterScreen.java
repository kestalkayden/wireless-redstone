package com.kestalkayden.wirelessredstone.client;

import com.kestalkayden.wirelessredstone.menu.TransmitterMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TransmitterScreen extends AbstractContainerScreen<TransmitterMenu> {

    private static final int GUI_W = 170;
    private static final int GUI_H = 96;

    private static final int BG_GRAY     = 0xFFC6C6C6;
    private static final int BG_BORDER   = 0xFF555555;
    private static final int LABEL_COLOR = 0xFF404040;

    private static final int LABEL_X    = 8;
    private static final int MINUS_X    = 78;
    private static final int FIELD_X    = 94;
    private static final int FIELD_W    = 44;
    private static final int PLUS_X     = 140;
    private static final int BUTTON_W   = 14;
    private static final int ROW_H      = 14;

    private static final int CHAN_Y     = 22;
    private static final int FREQ_Y     = 42;
    private static final int LOCK_Y     = 62;
    private static final int STATUS_Y   = 80;

    private static final int LOCK_BOX_SIZE  = 10;
    private static final int LOCK_BOX_Y     = LOCK_Y + 1;
    private static final int LOCK_LABEL_X   = LABEL_X + LOCK_BOX_SIZE + 4;

    private EditBox channelField;
    private EditBox frequencyField;
    private TinyCheckbox lockCheckbox;

    private Integer pendingChannel = null;
    private Integer pendingFrequency = null;
    private Boolean pendingLock = null;

    public TransmitterScreen(TransmitterMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, GUI_W, GUI_H);
        this.inventoryLabelY = -1000;  // no player inv visible; push label off-screen
    }

    @Override
    protected void init() {
        super.init();

        channelField = new EditBox(font, leftPos + FIELD_X, topPos + CHAN_Y,
            FIELD_W, ROW_H, Component.translatable("gui.wirelessredstone.channel"));
        channelField.setMaxLength(3);
        channelField.setValue(String.valueOf(menu.channel()));
        channelField.setResponder(this::sendChannel);
        addRenderableWidget(channelField);

        addRenderableWidget(Button.builder(Component.literal("-"), b -> stepChannel(-1))
            .bounds(leftPos + MINUS_X, topPos + CHAN_Y, BUTTON_W, ROW_H).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> stepChannel(1))
            .bounds(leftPos + PLUS_X, topPos + CHAN_Y, BUTTON_W, ROW_H).build());

        frequencyField = new EditBox(font, leftPos + FIELD_X, topPos + FREQ_Y,
            FIELD_W, ROW_H, Component.translatable("gui.wirelessredstone.frequency"));
        frequencyField.setMaxLength(5);
        frequencyField.setValue(String.valueOf(menu.frequency()));
        frequencyField.setResponder(this::sendFrequency);
        addRenderableWidget(frequencyField);

        addRenderableWidget(Button.builder(Component.literal("-"), b -> stepFrequency(-1))
            .bounds(leftPos + MINUS_X, topPos + FREQ_Y, BUTTON_W, ROW_H).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> stepFrequency(1))
            .bounds(leftPos + PLUS_X, topPos + FREQ_Y, BUTTON_W, ROW_H).build());

        lockCheckbox = new TinyCheckbox(leftPos + LABEL_X, topPos + LOCK_BOX_Y,
            LOCK_BOX_SIZE, menu.ownerLock(), this::sendOwnerLock);
        addRenderableWidget(lockCheckbox);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        syncFromMenu();
    }

    private void syncFromMenu() {
        if (channelField != null && !channelField.isFocused()) {
            int server = menu.channel();
            if (pendingChannel != null) {
                if (server == pendingChannel) pendingChannel = null;
                else return;
            }
            String s = String.valueOf(server);
            if (!s.equals(channelField.getValue())) channelField.setValue(s);
        }
        if (frequencyField != null && !frequencyField.isFocused()) {
            int server = menu.frequency();
            if (pendingFrequency != null) {
                if (server == pendingFrequency) pendingFrequency = null;
                else return;
            }
            String s = String.valueOf(server);
            if (!s.equals(frequencyField.getValue())) frequencyField.setValue(s);
        }
        if (lockCheckbox != null) {
            boolean server = menu.ownerLock();
            if (pendingLock != null) {
                if (server == pendingLock) pendingLock = null;
                else return;
            }
            if (lockCheckbox.isSelected() != server) lockCheckbox.setSelected(server);
        }
    }

    private void stepChannel(int delta) {
        int current;
        try { current = Integer.parseInt(channelField.getValue()); }
        catch (NumberFormatException e) { current = menu.channel(); }
        int next = Math.max(1, Math.min(128, current + delta));
        if (next != current) channelField.setValue(String.valueOf(next));
    }

    private void stepFrequency(int delta) {
        int current;
        try { current = Integer.parseInt(frequencyField.getValue()); }
        catch (NumberFormatException e) { current = menu.frequency(); }
        int next = Math.max(0, Math.min(65535, current + delta));
        if (next != current) frequencyField.setValue(String.valueOf(next));
    }

    private void sendChannel(String text) {
        if (minecraft == null || minecraft.gameMode == null) return;
        int v;
        if (text.isEmpty()) v = 1;
        else if (!text.matches("\\d{1,3}")) return;
        else {
            v = Integer.parseInt(text);
            if (v < 1 || v > 128) return;
        }
        pendingChannel = v;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, TransmitterMenu.encodeChan(v));
    }

    private void sendFrequency(String text) {
        if (minecraft == null || minecraft.gameMode == null) return;
        int v;
        if (text.isEmpty()) v = 0;
        else if (!text.matches("\\d{1,5}")) return;
        else {
            v = Integer.parseInt(text);
            if (v > 65535) return;
        }
        pendingFrequency = v;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, TransmitterMenu.encodeFreq(v));
    }

    private void sendOwnerLock(boolean value) {
        if (minecraft == null || minecraft.gameMode == null) return;
        pendingLock = value;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, TransmitterMenu.encodeLock(value));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + GUI_W, y + GUI_H, BG_GRAY);
        g.fill(x, y, x + GUI_W, y + 1, BG_BORDER);
        g.fill(x, y + GUI_H - 1, x + GUI_W, y + GUI_H, BG_BORDER);
        g.fill(x, y, x + 1, y + GUI_H, BG_BORDER);
        g.fill(x + GUI_W - 1, y, x + GUI_W, y + GUI_H, BG_BORDER);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.translatable("gui.wirelessredstone.channel").getString(),
            LABEL_X, CHAN_Y + 3, LABEL_COLOR, false);
        g.text(font, Component.translatable("gui.wirelessredstone.frequency").getString(),
            LABEL_X, FREQ_Y + 3, LABEL_COLOR, false);
        g.text(font, Component.translatable("gui.wirelessredstone.lock").getString(),
            LOCK_LABEL_X, LOCK_Y + 2, LABEL_COLOR, false);
        g.text(font,
            Component.translatable("gui.wirelessredstone.paired_with", menu.receiverCount()).getString(),
            LABEL_X, STATUS_Y, LABEL_COLOR, false);
    }
}
