package myau.mixin;

import myau.HourClient;
import myau.module.modules.ServerIPHider;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreenServerList.class)
public class MixinGuiScreenServerList {
    @Shadow private GuiTextField inputField;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo ci) {
        ServerIPHider serverIPHider = (ServerIPHider) HourClient.moduleManager.getModule(ServerIPHider.class);
        if (serverIPHider != null && serverIPHider.isEnabled()) {
            String ip = this.inputField.getText().toLowerCase();
            if (ip.endsWith(".com") || ip.endsWith(".net") || ip.endsWith(".xyz")) {
                this.inputField.setText("hourclient.qzz.io");
            }
        }
    }
}
