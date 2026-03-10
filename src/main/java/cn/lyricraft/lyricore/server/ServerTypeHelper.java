package cn.lyricraft.lyricore.server;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ServerTypeHelper {
    public static boolean isInnerServer(){
        return (Minecraft.getInstance().isLocalServer() || Minecraft.getInstance().getSingleplayerServer() != null);
    }

    public static boolean isLocalPlayer(Player player){
        if (!isInnerServer()) return false;
        Player local = Minecraft.getInstance().player;
        if (local == null) return false;
        return (player.getUUID() == local.getUUID());
    }
}
