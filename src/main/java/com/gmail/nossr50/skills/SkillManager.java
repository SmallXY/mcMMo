package com.gmail.nossr50.skills;

import org.bukkit.entity.Player;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.skills.PerksUtils;

public abstract class SkillManager {
    protected McMMOPlayer mcMMOPlayer;
    protected int activationChance;
    protected SkillType skill;

    public SkillManager(McMMOPlayer mcMMOPlayer, SkillType skill) {
        this.mcMMOPlayer = mcMMOPlayer;
        this.activationChance = PerksUtils.handleLuckyPerks(mcMMOPlayer.getPlayer(), skill);
        this.skill = skill;
    }

    public Player getPlayer() {
        return mcMMOPlayer.getPlayer();
    }

    public int getSkillLevel() {
        return mcMMOPlayer.getSkillLevel(skill);
    }

    public void applyXpGain(float xp) {
        mcMMOPlayer.beginXpGain(skill, xp);
    }
}
