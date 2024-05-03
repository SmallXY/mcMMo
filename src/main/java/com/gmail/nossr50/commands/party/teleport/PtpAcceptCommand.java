package com.gmail.nossr50.commands.party.teleport;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.party.PartyTeleportRecord;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.SkillUtils;

public class PtpAcceptCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!Permissions.partyTeleportAccept(sender)) {
            sender.sendMessage(command.getPermissionMessage());
            return true;
        }

        Player player = (Player) sender;
        PartyTeleportRecord ptpRecord = UserManager.getPlayer(player).getPartyTeleportRecord();

        if (!ptpRecord.hasRequest()) {
            player.sendMessage(LocaleLoader.getString("Commands.ptp.NoRequests"));
            return true;
        }

        if (SkillUtils.cooldownExpired(ptpRecord.getTimeout(), Config.getInstance().getPTPCommandTimeout())) {
            ptpRecord.removeRequest();
            player.sendMessage(LocaleLoader.getString("Commands.ptp.RequestExpired"));
            return true;
        }

        Player target = ptpRecord.getRequestor();
        ptpRecord.removeRequest();

        if (!PtpCommand.canTeleport(sender, player, target.getName())) {
            return true;
        }

        if (Config.getInstance().getPTPCommandWorldPermissions()) {
            World targetWorld = target.getWorld();
            World playerWorld = player.getWorld();

            if (!Permissions.partyTeleportAllWorlds(target)) {
                if (!Permissions.partyTeleportWorld(target, targetWorld)) {
                    target.sendMessage(LocaleLoader.getString("Commands.ptp.NoWorldPermissions", targetWorld.getName()));
                    return true;
                }
                else if (targetWorld != playerWorld && !Permissions.partyTeleportWorld(target, playerWorld)) {
                    target.sendMessage(LocaleLoader.getString("Commands.ptp.NoWorldPermissions", playerWorld.getName()));
                    return true;
                }
            }
        }

        PtpCommand.handleTeleportWarmup(target, player);
        return true;
    }
}
