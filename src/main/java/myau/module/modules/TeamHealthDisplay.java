package myau.module.modules;

import myau.module.Module;

import myau.event.EventTarget;
import myau.events.PacketEvent;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty; // Added import for ModeProperty
import myau.util.ChatUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TeamHealthDisplay extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final DecimalFormat df = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));

    public final BooleanProperty showHealth = new BooleanProperty("show-health", true);
    public final BooleanProperty showTicks = new BooleanProperty("show-ticks", true);
    public final BooleanProperty onlyTeam = new BooleanProperty("only-team", false);
    public final BooleanProperty onlyEnemy = new BooleanProperty("only-enemy", false);
    public final ModeProperty teamCheckMode = new ModeProperty("team-check-mode", 0, new String[]{"FRIEND_MANAGER", "SCOREBOARD_COLOR", "OFF"}); // New property

    public TeamHealthDisplay() {
        super("TeamHealthDisplay", false);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!event.isCancelled() && mc.thePlayer != null && mc.theWorld != null) {
            if (event.getPacket() instanceof S1CPacketEntityMetadata) {
                S1CPacketEntityMetadata packet = (S1CPacketEntityMetadata) event.getPacket();
                Entity entity = mc.theWorld.getEntityByID(packet.getEntityId());

                if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    EntityPlayer targetPlayer = (EntityPlayer) entity;

                    boolean isTeammate = false;
                    boolean isEnemy = false;

                    switch (this.teamCheckMode.getValue()) {
                        case 0: // FRIEND_MANAGER
                            isTeammate = TeamUtil.isFriend(targetPlayer);
                            isEnemy = !isTeammate && TeamUtil.isTarget(targetPlayer);
                            break;
                        case 1: // SCOREBOARD_COLOR
                            isTeammate = TeamUtil.isSameTeam(targetPlayer);
                            isEnemy = !isTeammate; // If not teammate by scoreboard color, assume enemy
                            break;
                        case 2: // OFF
                            isTeammate = false;
                            isEnemy = false;
                            break;
                    }

                    if (this.onlyTeam.getValue() && !isTeammate) return;
                    if (this.onlyEnemy.getValue() && !isEnemy) return;

                    // If team check mode is OFF, and neither onlyTeam nor onlyEnemy is enabled, still proceed.
                    // If team check is OFF, then isTeammate and isEnemy are both false, so onlyTeam and onlyEnemy filters will block all players.
                    // This implies if teamCheckMode is OFF, we should show for ALL players unless explicit filtering happens.
                    // Let's adjust this: if teamCheckMode is OFF, we treat everyone as a generic player unless onlyTeam/onlyEnemy is active based on FRIEND_MANAGER.

                    if (this.teamCheckMode.getValue() == 2) { // OFF
                        isTeammate = TeamUtil.isFriend(targetPlayer); // Still use friend manager for onlyTeam filter if mode is OFF
                        isEnemy = !isTeammate && TeamUtil.isTarget(targetPlayer); // Same for enemy
                    }


                    for (DataWatcher.WatchableObject watchableObject : packet.func_149376_c()) {
                        if (watchableObject.getDataValueId() == 6 && watchableObject.getObject() instanceof Float) { // Health ID
                            float newHealth = (Float) watchableObject.getObject();
                            float healthDifference = newHealth - targetPlayer.getHealth();

                            if (healthDifference != 0.0F) {
                                String healthColor = healthDifference > 0 ? "&a" : "&c";
                                String entityType;
                                if (isTeammate) {
                                    entityType = "Teammate";
                                } else if (isEnemy) {
                                    entityType = "Enemy";
                                } else {
                                    entityType = "Player";
                                }


                                StringBuilder message = new StringBuilder();
                                message.append(String.format("&7[%s] %s: %s%s", entityType, targetPlayer.getName(), healthColor, df.format(healthDifference)));
                                if (this.showTicks.getValue()) {
                                    message.append(String.format(" (&otick: %d&r)", mc.thePlayer.ticksExisted));
                                }
                                ChatUtil.sendFormatted(message.toString());
                            }
                        }
                    }
                }
            }
        }
    }
}

