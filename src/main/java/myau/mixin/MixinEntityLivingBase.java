package myau.mixin;

import myau.HourClient;
import myau.event.EventManager;
import myau.events.StrafeEvent;
import myau.management.RotationState;
import myau.module.modules.Jesus;
import myau.event.events.EventPlayerKill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP; // Re-added
import net.minecraft.enchantment.EnchantmentHelper; // Re-added
import net.minecraft.entity.Entity; // Re-added
import net.minecraft.entity.EntityLivingBase; // Re-added
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side; // Re-added
import net.minecraftforge.fml.relauncher.SideOnly; // Re-added
import org.spongepowered.asm.mixin.Mixin; // Re-added
import org.spongepowered.asm.mixin.injection.At; // Re-added
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable; // Re-added
import org.spongepowered.asm.mixin.injection.Redirect; // Re-added
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin(value = {EntityLivingBase.class}, priority = 9999)
public abstract class MixinEntityLivingBase extends MixinEntity {
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (Minecraft.getMinecraft().thePlayer != null && damageSource.getEntity() == Minecraft.getMinecraft().thePlayer) {
            EventManager.call(new EventPlayerKill((EntityLivingBase) (Object) this));
        }
    }

    @ModifyVariable(
            method = {"jump"},
            at = @At("STORE"),
            ordinal = 0
    )
    private float jump(float float1) {
        return (Entity) ((Object) this) instanceof EntityPlayerSP && RotationState.isActived()
                ? RotationState.getSmoothedYaw() * (float) (Math.PI / 180.0)
                : float1;
    }

    @Redirect(
            method = {"moveEntityWithHeading"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;moveFlying(FFF)V"
            )
    )
    private void moveEntityWithHeading(EntityLivingBase entityLivingBase, float float2, float float3, float float4) {
        if ((Entity) ((Object) this) instanceof EntityPlayerSP) {
            StrafeEvent event = new StrafeEvent(float2, float3, float4);
            EventManager.call(event);
            float2 = event.getStrafe();
            float3 = event.getForward();
            float4 = event.getFriction();
            boolean actived = RotationState.isActived();
            float yaw = this.rotationYaw;
            if (actived) {
                this.rotationYaw = RotationState.getSmoothedYaw();
            }
            entityLivingBase.moveFlying(float2, float3, float4);
            if (actived) {
                this.rotationYaw = yaw;
            }
        } else {
            entityLivingBase.moveFlying(float2, float3, float4);
        }
    }

    @ModifyVariable(
            method = {"moveEntityWithHeading"},
            name = {"f3"},
            at = @At("STORE")
    )
    private float moveEntityWithHeading(float float1) {
        if ((EntityLivingBase) ((Object) this) instanceof EntityPlayerSP && float1 == (float) EnchantmentHelper.getDepthStriderModifier((EntityLivingBase) ((Object) this))) {
            if (HourClient.moduleManager == null) {
                return float1;
            }
            Jesus jesus = (Jesus) HourClient.moduleManager.modules.get(Jesus.class);
            if (jesus.isEnabled() && (!jesus.groundOnly.getValue() || this.onGround)) {
                return Math.max(float1, jesus.speed.getValue());
            }
        }
        return float1;
    }
}
