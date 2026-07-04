package com.kestalkayden.wirelessredstone.access;

import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Owner-based edit gate. Both blocks record the UUID of whoever placed them; only that
 * owner may reconfigure the block (menu edits) or cycle its manual mode. Blocks with no
 * recorded owner (e.g. command/worldgen-placed) are public. Creative-mode players and
 * operators (permission level &gt;= 2) always bypass, matching vanilla protection semantics.
 */
public final class EditAccess {
    private EditAccess() {}

    /** True if {@code player} is allowed to mutate a block owned by {@code ownerUuid}. */
    public static boolean canEdit(Player player, UUID ownerUuid) {
        if (ownerUuid == null) return true;
        if (ownerUuid.equals(player.getUUID())) return true;
        if (player.getAbilities().instabuild) return true;   // creative
        // operator: consult the server op list (server is reachable via the ServerLevel).
        if (player instanceof ServerPlayer sp && sp.level() instanceof ServerLevel sl) {
            return sl.getServer().getPlayerList().isOp(sp.nameAndId());
        }
        return false;
    }

    /** Tells the player (via action bar) that a block they tried to edit isn't theirs. */
    public static void notifyDenied(Player player) {
        if (player instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundSetActionBarTextPacket(
                Component.translatable("message.wirelessredstone.locked_by_owner")));
        }
    }
}
