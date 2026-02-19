package myau.mixin;

import myau.HourClient;
import myau.module.modules.ServerIPHider;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerListEntryNormal.class)
public abstract class MixinServerListEntryNormal {

    @Shadow @Final private ServerData server;

    @Redirect(method = "drawEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I", ordinal = 0))
    private int redirectDrawServerName(FontRenderer fontRenderer, String text, int x, int y, int color) {
        ServerIPHider serverIPHider = (ServerIPHider) HourClient.moduleManager.getModule(ServerIPHider.class);
        if (serverIPHider != null && serverIPHider.isEnabled()) {
            String ip = this.server.serverIP.toLowerCase();
            String name = this.server.serverName.toLowerCase();
            if (ip.endsWith(".com") || ip.endsWith(".net") || ip.endsWith(".xyz") || name.endsWith(".com") || name.endsWith(".net") || name.endsWith(".xyz")) {
                return fontRenderer.drawString("hourclient.qzz.io", x, y, color);
            }
        }
        return fontRenderer.drawString(text, x, y, color);
    }
}
