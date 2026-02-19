package myau.ui.overlay.dynamicisland;

import myau.HourClient;
import myau.events.Render2DEvent;
import myau.module.modules.FPScounter;
import myau.module.modules.HUD;
import myau.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import javax.vecmath.Vector4d;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DynamicIsland {
    private static Minecraft mc = Minecraft.getMinecraft();
    private static FontRenderer fontRenderer = mc.fontRendererObj;

    private static List<IslandTrigger> TRIGGERS = new ArrayList<>();

    static {
        TRIGGERS.add(new ScaffoldIsland());
        TRIGGERS.add(new DefaultIsland());
    }

    // Animation state variables
    private static float animX, animY, animW, animH;
    private static boolean initialized = false;
    private static boolean targetHudRegistered = false; // Flag to ensure TargetHUD is registered only once

    public static void render(Render2DEvent event, float hudScale, int cornerRadius) {
        if (TRIGGERS.isEmpty()) return;

        TRIGGERS.sort((a, b) -> Integer.compare(b.getIslandPriority(), a.getIslandPriority()));

        IslandTrigger trigger = TRIGGERS.stream().filter(i -> i.isAvailable()).findFirst().orElse(TRIGGERS.get(0));

        ScaledResolution sr = new ScaledResolution(mc);
        float targetW = trigger.getIslandWidth();
        float targetH = trigger.getIslandHeight();
        float targetX = (sr.getScaledWidth() - targetW) / 2f;
        float targetY = 10;

        if (trigger instanceof CustomIslandTrigger) {
            targetX = ((CustomIslandTrigger) trigger).getIslandX();
            targetY = ((CustomIslandTrigger) trigger).getIslandY();
        }

        float speed = 0.15f;
        if (!initialized) {
            animX = targetX; animY = targetY; animW = targetW; animH = targetH;
            initialized = true;
        }
        animX += (targetX - animX) * speed;
        animY += (targetY - animY) * speed;
        animW += (targetW - animW) * speed;
        animH += (targetH - animH) * speed;

        float x = animX / hudScale;
        float y = animY / hudScale;
        float w = animW / hudScale;
        float h = animH / hudScale;

        // Fetch corner radius from FPScounter
        int cornerRadiusValue = ((Number) ((FPScounter) HourClient.moduleManager.getModule(FPScounter.class)).cornerRadius.getValue()).intValue();
        int clampedCornerRadius = Math.min(cornerRadiusValue, (int) Math.min(w, h) / 2);

        RenderUtil.enableRenderState();
        
        // Draw rounded outline
        int outlineColor = new Color(0, 0, 0, 100).getRGB(); // Same as original outline color
        RenderUtil.drawRoundedRect((int)(x - 0.5f), (int)(y - 0.5f), (int)(w + 1.0f), (int)(h + 1.0f), clampedCornerRadius, outlineColor);

        // Draw rounded background
        RenderUtil.drawRoundedRect((int)x, (int)y, (int)w, (int)h, clampedCornerRadius, new Color(10, 10, 10, 200).getRGB());

        // Draw top bar (keep as rect for now, as rounding a thin bar is tricky)
        RenderUtil.drawRect(x, y, x + w, y + 1.5f, new Color(255, 255, 255, 100).getRGB());

        RenderUtil.disableRenderState();

        float progress = Math.min(1.0f, animW / targetW);
        trigger.renderIsland(event, x, y, w, h, progress);
    }

    public static void addTrigger(IslandTrigger trigger) {
        TRIGGERS.add(trigger);
        TRIGGERS.sort(Comparator.comparingInt(IslandTrigger::getIslandPriority).reversed());
    }

    public static void addIsland(IslandTrigger trigger) {
        addTrigger(trigger);
    }
}
