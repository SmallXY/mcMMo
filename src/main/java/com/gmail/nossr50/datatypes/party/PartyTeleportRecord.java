package com.gmail.nossr50.datatypes.party;

import org.bukkit.entity.Player;

import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.util.Misc;

public class PartyTeleportRecord {
    private Player  requestor;
    private boolean enabled, confirmRequired;
    private int     timeout, lastUse;

    public PartyTeleportRecord() {
        requestor = null;
        enabled = true;
        confirmRequired = Config.getInstance().getPTPCommandConfirmRequired();
        timeout = 0;
        lastUse = 0;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }

    public Player getRequestor() {
        return requestor;
    }

    public void setRequestor(Player requestor) {
        this.requestor = requestor;
    }

    public boolean hasRequest() {
        return (requestor != null);
    }

    public void removeRequest() {
        requestor = null;
    }

    public boolean isConfirmRequired() {
        return confirmRequired;
    }

    public void toggleConfirmRequired() {
        confirmRequired = !confirmRequired;
    }

    public int getLastUse() {
        return lastUse;
    }

    public void actualizeLastUse() {
        lastUse = (int) (System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR);
    }

    public int getTimeout() {
        return timeout;
    }

    public void actualizeTimeout() {
        timeout = (int) (System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR);
    }
}
