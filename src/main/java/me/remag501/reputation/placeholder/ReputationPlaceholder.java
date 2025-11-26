package me.remag501.reputation.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.remag501.reputation.manager.ReputationManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReputationPlaceholder extends PlaceholderExpansion {

    private final ReputationManager reputationManager;

    public ReputationPlaceholder(ReputationManager reputationManager) {
        this.reputationManager = reputationManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "reputation";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Remag501";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // survives /papi reload
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        // %reputation_<dealer>%
        String dealer = params.toLowerCase();

        if (!reputationManager.isValidDealer(dealer)) {
            return "0";
        }

        return String.valueOf(reputationManager.getReputation(player, dealer));
    }
}
