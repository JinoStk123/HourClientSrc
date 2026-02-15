package myau.module.modules;

import myau.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import myau.event.EventTarget;
import myau.events.Render3DEvent;

import myau.property.properties.*;

import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BedSurroundInfo extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Map<BlockPos, String> bedInfo = new HashMap<>();

    public final IntProperty detectionRadius = new IntProperty("detection-radius", 10, 5, 30);

    public BedSurroundInfo() {
        super("BedSurroundInfo", false);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        bedInfo.clear();
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        bedInfo.clear();
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        bedInfo.clear();

        int radius = this.detectionRadius.getValue(); // Search radius for beds
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (block instanceof BlockBed) {
                        BlockBed bedBlock = (BlockBed) block;
                        // Ensure we only process one part of the bed (e.g., the head)
                        if (mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                            detectSurroundingBlocks(pos);
                        }
                    }
                }
            }
        }

        renderBedInformation();
    }

    private void detectSurroundingBlocks(BlockPos bedPos) {
        Map<Block, Integer> surroundingBlockCounts = new HashMap<>();
        int radius = 2; 

        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) { // Changed y to start from 0 (bed's level)
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos neighborPos = bedPos.add(x, y, z);
                    Block neighborBlock = mc.theWorld.getBlockState(neighborPos).getBlock();

                    if (neighborBlock != Blocks.air) {
                        surroundingBlockCounts.merge(neighborBlock, 1, Integer::sum);
                    }
                }
            }
        }

        StringBuilder infoBuilder = new StringBuilder();
        if (surroundingBlockCounts.isEmpty()) {
            infoBuilder.append("No surrounding blocks");
        } else {
            infoBuilder.append("Surrounds: ");
            surroundingBlockCounts.entrySet().stream()
                .sorted(Map.Entry.<Block, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String blockName = entry.getKey().getLocalizedName();
                    infoBuilder.append(blockName).append(" (").append(entry.getValue()).append("), ");
                });
            if (infoBuilder.length() > "Surrounds: ".length()) {
                infoBuilder.setLength(infoBuilder.length() - 2);
            }
        }
        bedInfo.put(bedPos, infoBuilder.toString());
    }

    private void renderBedInformation() {
        for (Map.Entry<BlockPos, String> entry : bedInfo.entrySet()) {
            BlockPos bedPos = entry.getKey();
            String info = entry.getValue();

            double x = bedPos.getX() + 0.5 - mc.getRenderManager().viewerPosX;
            double y = bedPos.getY() + 1.5 - mc.getRenderManager().viewerPosY; // Render above the bed
            double z = bedPos.getZ() + 0.5 - mc.getRenderManager().viewerPosZ;

            renderString(info, x, y, z, Color.WHITE.getRGB());
        }
    }

    private void renderString(String text, double x, double y, double z, int color) {
        float scale = 0.0267F; // Adjust scale as needed
        float offset = (float) (-mc.fontRendererObj.getStringWidth(text) / 2);

        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.translate(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        GlStateManager.enableTexture2D(); // Enable texture for font rendering
        mc.fontRendererObj.drawString(text, (int) offset, 0, color);
        GlStateManager.disableTexture2D(); // Disable texture after font rendering
        
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
    }
}
