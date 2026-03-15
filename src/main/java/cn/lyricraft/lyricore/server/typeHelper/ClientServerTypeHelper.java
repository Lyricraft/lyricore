package cn.lyricraft.lyricore.server.typeHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientServerTypeHelper {
    protected static boolean isInnerServer(){
        return (Minecraft.getInstance().isLocalServer() || Minecraft.getInstance().getSingleplayerServer() != null);
    }

    public static boolean isLocalPlayer(Player player){
        if (!isInnerServer()) return false;
        Player local = net.minecraft.client.Minecraft.getInstance().player;
        if (local == null) return false;
        return (player.getUUID().equals(local.getUUID()));
    }
}
