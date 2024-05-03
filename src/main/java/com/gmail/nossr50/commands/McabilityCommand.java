package com.gmail.nossr50.commands;

import org.bukkit.command.CommandSender;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.Permissions;

public class McabilityCommand extends ToggleCommand {
    @Override
    protected boolean hasOtherPermission(CommandSender sender) {
        return Permissions.mcabilityOthers(sender);
    }

    @Override
    protected boolean hasSelfPermission(CommandSender sender) {
        return Permissions.mcability(sender);
    }

    @Override
    protected void applyCommandAction(McMMOPlayer mcMMOPlayer) {
        mcMMOPlayer.getPlayer().sendMessage(LocaleLoader.getString("Commands.Ability." + (mcMMOPlayer.getAbilityUse() ? "Off" : "On")));
        mcMMOPlayer.toggleAbilityUse();
    }

    @Override
    protected void sendSuccessMessage(CommandSender sender, String playerName) {
        sender.sendMessage(LocaleLoader.getString("Commands.Ability.Toggle", playerName));
    }
}
