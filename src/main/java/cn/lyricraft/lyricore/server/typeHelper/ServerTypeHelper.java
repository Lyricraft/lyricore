package cn.lyricraft.lyricore.server.typeHelper;

import net.minecraft.world.entity.player.Player;

public class ServerTypeHelper {
    public static boolean isInnerServer(){
        if (!net.neoforged.fml.loading.FMLLoader.getDist().isClient()) {
            return false;
        } // 防止专用服务端加载后续内容
        return (ClientServerTypeHelper.isInnerServer());
    }

    public static boolean isLocalPlayer(Player player){
        if (!net.neoforged.fml.loading.FMLLoader.getDist().isClient()) {
            return false;
        } // 防止专用服务端加载后续内容
        return (ClientServerTypeHelper.isLocalPlayer(player));
    }
}
