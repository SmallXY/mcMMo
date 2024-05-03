package com.gmail.nossr50.events.skills.abilities;

import org.bukkit.entity.Player;

import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.skills.McMMOPlayerSkillEvent;

public class McMMOPlayerAbilityEvent extends McMMOPlayerSkillEvent {
    private AbilityType ability;

    protected McMMOPlayerAbilityEvent(Player player, SkillType skill) {
        super(player, skill);
        ability = skill.getAbility();
    }

    public AbilityType getAbility() {
        return ability;
    }
}
