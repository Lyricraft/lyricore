package cn.lyricraft.lyricore.log;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class LogHelper {
    public static String playerProfile(@Nullable Player player){
        if (player == null) return "<UNKNOWN>";
        else return (player.getName().toString() + " (" + player.getUUID() + ")");
    }
}
