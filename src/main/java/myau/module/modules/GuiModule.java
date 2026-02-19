package myau.module.modules;

import myau.module.Module;
import myau.property.properties.ColorProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.PercentProperty;
import myau.ui.ClickGui;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import myau.util.ChatUtil;

import java.awt.*;

public class GuiModule extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final IntProperty cornerRadius = new IntProperty("corner-radius", 16, 0, 20);
    public final ColorProperty backgroundColor = new ColorProperty("background-color", new Color(0, 0, 0).getRGB());
    public final PercentProperty backgroundOpacity = new PercentProperty("background-opacity", 40);
    public final ColorProperty panelColor = new ColorProperty("panel-color", new Color(0, 0, 0).getRGB());
    public final PercentProperty panelOpacity = new PercentProperty("panel-opacity", 40);

    public GuiModule() {
        super("ClickGui", false);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnabled() {
        setEnabled(false);
        mc.displayGuiScreen(ClickGui.getInstance());
    }
}
