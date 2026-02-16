package myau.module.modules;

import myau.HourClient;
import myau.event.EventTarget;
import myau.events.Render3DEvent;
import myau.module.Module;
import myau.property.properties.ColorProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;

public class Trail extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"History", "Calculate"});
    public final FloatProperty lifetime = new FloatProperty("lifetime", 20.0F, 5.0F, 100.0F);
    public final FloatProperty width = new FloatProperty("width", 2.0F, 0.5F, 5.0F);
    public final ColorProperty color = new ColorProperty("color", Color.WHITE.getRGB());

    private final LinkedList<Vec3> historyPoints = new LinkedList<>();

    public Trail() {
        super("Trail", false);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;

        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        // Update history points
        if (this.mode.getValue() == 0) { // History mode
            historyPoints.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            while (historyPoints.size() > lifetime.getValue()) {
                historyPoints.removeFirst();
            }
        } else { // Calculate mode, clear history if mode changes
            historyPoints.clear();
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableDepth();
        GL11.glLineWidth(width.getValue());
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        GL11.glBegin(GL11.GL_LINE_STRIP);

        Color c = new Color(color.getValue());

        if (this.mode.getValue() == 0) { // History mode rendering
            int i = 0;
            for (Vec3 point : historyPoints) {
                float alpha = (float) i / historyPoints.size();
                GL11.glColor4f(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, alpha * c.getAlpha() / 255.0F);
                GL11.glVertex3d(point.xCoord - renderPosX, point.yCoord - renderPosY, point.zCoord - renderPosZ);
                i++;
            }
        } else { // Calculate mode rendering
            // Simplified calculation: just draw a line in front of the player
            // A more complex calculation would involve predicting player velocity/path
            Vec3 playerPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            double motionX = mc.thePlayer.motionX;
            double motionY = mc.thePlayer.motionY;
            double motionZ = mc.thePlayer.motionZ;

            // Draw current position
            GL11.glColor4f(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F);
            GL11.glVertex3d(playerPos.xCoord - renderPosX, playerPos.yCoord - renderPosY, playerPos.zCoord - renderPosZ);

            // Predict future positions
            for (float f = 0.1F; f <= lifetime.getValue() / 10.0F; f += 0.1F) { // Shorter lifetime for calculate mode
                double nextX = playerPos.xCoord + motionX * f;
                double nextY = playerPos.yCoord + motionY * f;
                double nextZ = playerPos.zCoord + motionZ * f;
                float alpha = (1.0F - (f / (lifetime.getValue() / 10.0F))); // Fade out
                GL11.glColor4f(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, alpha * c.getAlpha() / 255.0F);
                GL11.glVertex3d(nextX - renderPosX, nextY - renderPosY, nextZ - renderPosZ);
            }
        }


        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public void onDisable() {
        historyPoints.clear();
    }
}
