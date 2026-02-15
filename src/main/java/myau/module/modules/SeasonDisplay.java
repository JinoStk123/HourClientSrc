package myau.module.modules;

import myau.event.EventManager;
import myau.event.EventTarget;
import myau.event.events.EventPlayerKill;
import myau.events.Render2DEvent; // Correct import for Render2DEvent
import myau.module.Module;
import myau.property.properties.LongProperty;
import myau.property.properties.IntProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class SeasonDisplay extends Module {
    public final LongProperty seasonStartTime = new LongProperty("Season Start Time", System.currentTimeMillis(), Long.MIN_VALUE, Long.MAX_VALUE);
    public final IntProperty killsCount = new IntProperty("Kills", 0, 0, Integer.MAX_VALUE);

    private final Minecraft mc = Minecraft.getMinecraft();

    public SeasonDisplay() {
        super("SeasonDisplay", false, true); // defaultPersistent should be true
    }

    @Override
    public void onEnabled() {
        EventManager.register(this);
        if (seasonStartTime.getValue() == 0) {
            seasonStartTime.setValue(System.currentTimeMillis());
            killsCount.setValue(0);
        }
    }

    @Override
    public void onDisabled() {
        EventManager.unregister(this);
    }

    @EventTarget
    public void onPlayerKill(EventPlayerKill event) {
        killsCount.setValue(killsCount.getValue() + 1);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) { // Changed to Render2DEvent
        if (!isEnabled()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;

        long currentTime = System.currentTimeMillis();
        long durationMillis = currentTime - seasonStartTime.getValue();

        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        String duration = String.format("Time: %d d %02d h %02d m %02d s", days, hours, minutes, seconds);
        String kills = String.format("Kills: %d", killsCount.getValue());

        int x = 5;
        int y = 5;

        fr.drawStringWithShadow(duration, x, y, 0xFFFFFF); // White color
        fr.drawStringWithShadow(kills, x, y + fr.FONT_HEIGHT + 2, 0xFFFFFF); // White color
    }
}
