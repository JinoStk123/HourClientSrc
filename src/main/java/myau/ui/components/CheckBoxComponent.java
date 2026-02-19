
package myau.ui.components;

import myau.HourClient;
import myau.enums.ChatColors;
import myau.module.modules.HUD;
import myau.property.properties.BooleanProperty;
import myau.ui.Component;
import myau.util.RenderUtil;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckBoxComponent implements Component {
    private final BooleanProperty property;
    private final ModuleComponent module;
    private int offsetY;
    private int x;
    private int y;

    public CheckBoxComponent(BooleanProperty property, ModuleComponent parentModule, int offsetY) {
        this.property = property;
        this.module = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }


    public void draw(AtomicInteger offset) {
        float cx = this.module.category.getX() + 4;
        float cy = this.module.category.getY() + this.offsetY + 4;
        float size = 6;
        
        RenderUtil.drawRoundedRect(cx, cy, size, size, 1.5f, new Color(0, 0, 0, 100).getRGB());
        if (this.property.getValue()) {
            RenderUtil.drawRoundedRect(cx + 1, cy + 1, size - 2, size - 2, 1f, ((HUD) HourClient.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis(), offset.get()).getRGB());
        }

        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        Minecraft.getMinecraft().fontRendererObj.drawString(this.property.getName().replace("-", " ") + ": " + ChatColors.formatColor(this.property.formatValue()), (cx + size + 2) * 2, (this.module.category.getY() + this.offsetY + 5) * 2, -1, false);
        GL11.glPopMatrix();
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.module.category.getY() + this.offsetY;
        this.x = this.module.category.getX();
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.module.panelExpand) {
            this.property.setValue(!this.property.getValue());
        }

    }

    @Override
    public void mouseReleased(int x, int y, int button) {

    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {

    }

    public boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.module.category.getWidth() && y > this.y && y < this.y + 11;
    }


    @Override
    public boolean isVisible() {
        return property.isVisible();
    }
}
