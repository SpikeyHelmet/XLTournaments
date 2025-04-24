package net.zithium.tournaments.action;

import net.zithium.tournaments.XLTournamentsPlugin;
import net.zithium.tournaments.action.actions.BroadcastMessageAction;
import net.zithium.tournaments.action.actions.CommandAction;
import net.zithium.tournaments.action.actions.ConsoleCommandAction;
import net.zithium.tournaments.action.actions.MessageAction;
import net.zithium.tournaments.action.actions.SoundAction;
import net.zithium.tournaments.tournament.Tournament;
import net.zithium.tournaments.utility.TextUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionManager {
    private final XLTournamentsPlugin plugin;
    private Map<String, Action> actions;

    public ActionManager(XLTournamentsPlugin plugin) {
        this.plugin = plugin;
    }



    public void onEnable() {
        actions = new HashMap<>();

        registerAction(
                new MessageAction(),
                new BroadcastMessageAction(),
                new CommandAction(),
                new ConsoleCommandAction(),
                new SoundAction()
        );
    }

    public void registerAction(Action... actions) {
        Arrays.asList(actions).forEach(action -> this.actions.put(action.getIdentifier(), action));
    }

    public void executeActions(Player player, List<String> items) {
        executeActions(player, items, null);
    }

    public void executeActions(Player player, List<String> items, Tournament tournament) {
        items.forEach(item -> {

            String actionName = StringUtils.substringBetween(item, "[", "]");
            if(actionName != null) {
                actionName = actionName.toUpperCase();
                Action action = actionName.isEmpty() ? null : actions.get(actionName);

                if (action != null) {
                    item = item.contains(" ") ? item.split(" ", 2)[1] : "";
                    if (player != null) {
                        item = item.replace("{PLAYER}", player.getName());
                        if (tournament != null) {
                            item = TextUtil.setPlaceholders(item, player, tournament);
                        } else {
                            item = TextUtil.setPlaceholders(item, player);
                        }

                        action.execute(plugin, player, item);
                    } else if (action.getIdentifier().equals("BROADCAST")) {
                        if (tournament != null) {
                            item = TextUtil.setPlaceholders(item, null, tournament);
                        } else {
                            item = TextUtil.setPlaceholders(item, null);
                        }

                        action.execute(plugin, player, item);
                    } else if (item.contains("{PLAYER}")) {
                        for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            item = item.replace("{PLAYER}", onlinePlayer.getName());
                            if (tournament != null) {
                                item = TextUtil.setPlaceholders(item, onlinePlayer, tournament);
                            } else {
                                item = TextUtil.setPlaceholders(item, onlinePlayer);
                            }

                            action.execute(plugin, onlinePlayer, item);
                        }
                    } else {
                        if (tournament != null) {
                            item = TextUtil.setPlaceholders(item, null, tournament);
                        } else {
                            item = TextUtil.setPlaceholders(item, null);
                        }

                        action.execute(plugin, null, item);
                    }
                }
            }
        });
    }
}
