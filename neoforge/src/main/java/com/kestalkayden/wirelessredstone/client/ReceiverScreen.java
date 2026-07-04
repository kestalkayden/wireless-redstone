package com.kestalkayden.wirelessredstone.client;

import com.kestalkayden.wirelessredstone.menu.ReceiverMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ReceiverScreen extends AbstractContainerScreen<ReceiverMenu> {

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
    private static final int CHECK_Y    = 62;
    private static final int STATUS_Y   = 80;

    private static final int CHECK_BOX_SIZE = 10;
    private static final int CHECK_BOX_Y    = CHECK_Y + 1;
    private static final int LOCK_BOX_X     = 8;
    private static final int LOCK_LABEL_X   = LOCK_BOX_X + CHECK_BOX_SIZE + 4;
    private static final int PRIVATE_BOX_X  = 80;
    private static final int PRIVATE_LABEL_X = PRIVATE_BOX_X + CHECK_BOX_SIZE + 4;

    private EditBox channelField;
    private EditBox frequencyField;
    private Button chanMinus, chanPlus, freqMinus, freqPlus;
    private TinyCheckbox editLockCheckbox;
    private TinyCheckbox privateCheckbox;

    private Integer pendingChannel = null;
    private Integer pendingFrequency = null;
    private Boolean pendingPrivate = null;
    private Boolean pendingEditLock = null;

    public ReceiverScreen(ReceiverMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, GUI_W, GUI_H);
        this.inventoryLabelY = -1000;
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

        chanMinus = Button.builder(Component.literal("-"), b -> stepChannel(-1))
            .bounds(leftPos + MINUS_X, topPos + CHAN_Y, BUTTON_W, ROW_H).build();
        addRenderableWidget(chanMinus);
        chanPlus = Button.builder(Component.literal("+"), b -> stepChannel(1))
            .bounds(leftPos + PLUS_X, topPos + CHAN_Y, BUTTON_W, ROW_H).build();
        addRenderableWidget(chanPlus);

        frequencyField = new EditBox(font, leftPos + FIELD_X, topPos + FREQ_Y,
            FIELD_W, ROW_H, Component.translatable("gui.wirelessredstone.frequency"));
        frequencyField.setMaxLength(5);
        frequencyField.setValue(String.valueOf(menu.frequency()));
        frequencyField.setResponder(this::sendFrequency);
        addRenderableWidget(frequencyField);

        freqMinus = Button.builder(Component.literal("-"), b -> stepFrequency(-1))
            .bounds(leftPos + MINUS_X, topPos + FREQ_Y, BUTTON_W, ROW_H).build();
        addRenderableWidget(freqMinus);
        freqPlus = Button.builder(Component.literal("+"), b -> stepFrequency(1))
            .bounds(leftPos + PLUS_X, topPos + FREQ_Y, BUTTON_W, ROW_H).build();
        addRenderableWidget(freqPlus);

        editLockCheckbox = new TinyCheckbox(leftPos + LOCK_BOX_X, topPos + CHECK_BOX_Y,
            CHECK_BOX_SIZE, menu.editLock(), this::sendEditLock);
        addRenderableWidget(editLockCheckbox);

        privateCheckbox = new TinyCheckbox(leftPos + PRIVATE_BOX_X, topPos + CHECK_BOX_Y,
            CHECK_BOX_SIZE, menu.privateMode(), this::sendPrivate);
        addRenderableWidget(privateCheckbox);

        applyEditableState();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        syncFromMenu();
        applyEditableState();
    }

    private void applyEditableState() {
        boolean canEdit = !menu.editLock();
        if (channelField != null)   channelField.setEditable(canEdit);
        if (frequencyField != null) frequencyField.setEditable(canEdit);
        if (chanMinus != null)  chanMinus.active = canEdit;
        if (chanPlus != null)   chanPlus.active = canEdit;
        if (freqMinus != null)  freqMinus.active = canEdit;
        if (freqPlus != null)   freqPlus.active = canEdit;
        if (privateCheckbox != null) privateCheckbox.active = canEdit;
    }

    private void syncFromMenu() {
        // Each field syncs independently — a pending edit on one must not skip the others
        // (an early return here used to freeze the private/lock checkboxes while a freq or
        // channel change was still in flight).
        if (channelField != null && !channelField.isFocused()) {
            int server = menu.channel();
            boolean pending = false;
            if (pendingChannel != null) {
                if (server == pendingChannel) pendingChannel = null;
                else pending = true;
            }
            if (!pending) {
                String s = String.valueOf(server);
                if (!s.equals(channelField.getValue())) channelField.setValue(s);
            }
        }
        if (frequencyField != null && !frequencyField.isFocused()) {
            int server = menu.frequency();
            boolean pending = false;
            if (pendingFrequency != null) {
                if (server == pendingFrequency) pendingFrequency = null;
                else pending = true;
            }
            if (!pending) {
                String s = String.valueOf(server);
                if (!s.equals(frequencyField.getValue())) frequencyField.setValue(s);
            }
        }
        if (privateCheckbox != null) {
            boolean server = menu.privateMode();
            boolean pending = false;
            if (pendingPrivate != null) {
                if (server == pendingPrivate) pendingPrivate = null;
                else pending = true;
            }
            if (!pending && privateCheckbox.isSelected() != server) privateCheckbox.setSelected(server);
        }
        if (editLockCheckbox != null) {
            boolean server = menu.editLock();
            boolean pending = false;
            if (pendingEditLock != null) {
                if (server == pendingEditLock) pendingEditLock = null;
                else pending = true;
            }
            if (!pending && editLockCheckbox.isSelected() != server) editLockCheckbox.setSelected(server);
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
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ReceiverMenu.encodeChan(v));
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
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ReceiverMenu.encodeFreq(v));
    }

    private void sendPrivate(boolean value) {
        if (minecraft == null || minecraft.gameMode == null) return;
        pendingPrivate = value;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ReceiverMenu.encodePrivate(value));
    }

    private void sendEditLock(boolean value) {
        if (minecraft == null || minecraft.gameMode == null) return;
        pendingEditLock = value;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ReceiverMenu.encodeEditLock(value));
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
            LOCK_LABEL_X, CHECK_Y + 2, LABEL_COLOR, false);
        g.text(font, Component.translatable("gui.wirelessredstone.private").getString(),
            PRIVATE_LABEL_X, CHECK_Y + 2, LABEL_COLOR, false);
        g.text(font,
            Component.translatable("gui.wirelessredstone.receiving_from",
                menu.transmitterCount(), menu.strength()).getString(),
            LABEL_X, STATUS_Y, LABEL_COLOR, false);
    }
}
