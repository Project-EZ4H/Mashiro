package me.liuli.mashiro.inject.mixins.entity;

import me.liuli.mashiro.Mashiro;
import me.liuli.mashiro.event.AttackEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;syncCurrentPlayItem()V"))
    private void attackEntity(EntityPlayer entityPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
        if(targetEntity==null)
            return;

        Mashiro.eventManager.callEvent(new AttackEvent(targetEntity));
    }
}
