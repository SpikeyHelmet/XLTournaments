package net.zithium.tournaments.command;

import net.zithium.tournaments.XLTournamentsPlugin;
import net.zithium.tournaments.objective.XLObjective;
import net.zithium.tournaments.tournament.Tournament;
import net.zithium.tournaments.tournament.TournamentBuilder;
import net.zithium.tournaments.tournament.TournamentManager;
import net.zithium.tournaments.tournament.TournamentStatus;
import net.zithium.tournaments.config.Messages;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import net.zithium.tournaments.utility.ColorUtil;
import net.zithium.tournaments.utility.TextUtil;
import net.zithium.tournaments.utility.Timeline;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Command("tournament")
@SuppressWarnings("unused")
public class TournamentsCommand extends CommandBase {

    private final XLTournamentsPlugin plugin;

    public TournamentsCommand(XLTournamentsPlugin plugin) {
        this.plugin = plugin;
        List<String> aliases = plugin.getConfig().getStringList("command_aliases");
        if (!aliases.isEmpty()) {
            this.setAliases(plugin.getConfig().getStringList("command_aliases"));
        }
    }

    @Default
    public void defaultCommand(final Player player) {
        plugin.getMenuManager().getTournamentGui().openInventory(player);
    }

    @SubCommand("help")
    @Permission({"tournaments.admin", "tournaments.command.help"})
    @WrongUsage("&c/tournament help")
    public void helpSubCommand(final CommandSender sender) {
        for (String s : plugin.getMessagesFile().getConfig().getStringList("general.help")) {
            sender.sendMessage(ColorUtil.color(s).replace("{VERSION}", plugin.getDescription().getVersion()));
        }
    }

    @SubCommand("reload")
    @Permission({"tournaments.admin", "tournaments.command.reload"})
    @WrongUsage("&c/tournament reload")
    public void reloadSubCommand(final CommandSender sender) {
        plugin.reload();
        Messages.RELOAD.send(sender);
    }

    @SubCommand("about")
    @WrongUsage("&c/tournament about")
    public void aboutSubCommand(final CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ColorUtil.color("&b&lXLTournaments"));
        sender.sendMessage(ColorUtil.color("&bVersion: &fv" + plugin.getDescription().getVersion()));
        sender.sendMessage(ColorUtil.color("&bAuthor: &fZithium Studios"));

        if (!TextUtil.isValidDownload()) {
            sender.sendMessage(ColorUtil.color("&4Registered to: &cFailed to find licensed owner to this plugin. Contact developer to report possible leak (itzsave)."));
        } else if (TextUtil.isMCMarket()) {
            sender.sendMessage(ColorUtil.color("&4Registered to: &chttps://builtbybit.com/members/%%__USER__%%/"));
        } else {
            sender.sendMessage(ColorUtil.color("&4Registered to: &chttps://www.spigotmc.org/members/%%__USER__%%/"));
        }
        sender.sendMessage("");
    }

    @SubCommand("update")
    @Permission({"tournaments.admin", "tournaments.command.update"})
    @WrongUsage("&c/tournament update")
    public void updateSubCommand(final CommandSender sender) {
        for (Tournament tournament : plugin.getTournamentManager().getTournaments()) {
            if (tournament.getStatus() == TournamentStatus.ACTIVE) {
                tournament.update();
            }
        }
        Messages.FORCE_UPDATED_TOURNAMENTS.send(sender);
    }

    @SubCommand("info")
    @Permission({"tournaments.admin", "tournaments.command.info"})
    @WrongUsage("&c/tournament info <tournament>")
    @Completion("#tournaments")
    public void infoSubCommand(final CommandSender sender, final String input) {
        Optional<Tournament> optionalTournament = plugin.getTournamentManager().getTournament(input);
        if (optionalTournament.isEmpty()) {
            sender.sendMessage(ColorUtil.color("&cCould not find tournament with that ID"));
            return;
        }

        Tournament tournament = optionalTournament.get();

        sender.sendMessage("");
        sender.sendMessage(ColorUtil.color("&b&lTournament Information"));
        sender.sendMessage("");
        sender.sendMessage(ColorUtil.color("&bIdentifier: &f" + tournament.getIdentifier()));
        sender.sendMessage(ColorUtil.color("&bStatus: &f" + tournament.getStatus().toString()));
        sender.sendMessage(ColorUtil.color("&bParticipants Amount: &f" + tournament.getParticipants().size()));
        sender.sendMessage(ColorUtil.color("&bObjective: &f" + tournament.getObjective().getIdentifier()));
        sender.sendMessage(ColorUtil.color("&bTimeline: &f" + tournament.getTimeline()));
        sender.sendMessage(ColorUtil.color("&bTimezone: &f" + tournament.getZoneId().getId()));
        sender.sendMessage(ColorUtil.color("&bStart Date: &f" + DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm:ss").format(tournament.getStartDate())));
        sender.sendMessage(ColorUtil.color("&bEnd Date: &f" + DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm:ss").format(tournament.getEndDate())));
        sender.sendMessage(ColorUtil.color("&bDisabled Worlds: &f" + tournament.getDisabledWorlds()));
        sender.sendMessage(ColorUtil.color("&bDisabled Gamemodes: &f" + tournament.getDisabledGamemodes()));
        sender.sendMessage(ColorUtil.color("&bAutomatic Participation: &f" + tournament.isAutomaticParticipation()));
        sender.sendMessage(ColorUtil.color("&bParticipation Cost: &f" + tournament.getParticipationCost()));
        org.bukkit.permissions.Permission permission = tournament.getParticipationPermission();
        sender.sendMessage(ColorUtil.color("&bParticipation Permission: &f" + (permission == null ? "N/A" : permission.getName())));
        sender.sendMessage(ColorUtil.color("&bLeaderboard Refresh: &f" + tournament.getLeaderboardRefresh()));
        Set<String> metadata = tournament.getMeta().keySet();
        sender.sendMessage(ColorUtil.color("&bMetadata: &f" + (metadata.isEmpty() ? "N/A" : metadata)));
        sender.sendMessage("");
    }

    @SubCommand("clear")
    @Permission({"tournaments.admin", "tournaments.command.clear"})
    @WrongUsage("&c/tournament clear <tournament>")
    @Completion("#tournaments")
    public void clearSubCommand(final CommandSender sender, final String input) {
        Optional<Tournament> optionalTournament = plugin.getTournamentManager().getTournament(input);
        if (optionalTournament.isEmpty()) {
            sender.sendMessage(ColorUtil.color("&cCould not find tournament with that ID"));
            return;
        }

        Tournament tournament = optionalTournament.get();
        tournament.clearParticipants();
        Messages.TOURNAMENT_CLEARED.send(sender, "{TOURNAMENT}", tournament.getIdentifier());
    }

    @SubCommand("clearplayer")
    @Permission({"tournaments.admin", "tournaments.command.clearplayer"})
    @WrongUsage("&c/tournament clearplayer <player> <tournament>")
    @Completion({"#players", "#tournaments"})
    public void clearPlayerSubCommand(final CommandSender sender, final Player target, final String input) {

        if (target == null) {
            sender.sendMessage(ColorUtil.color("&cPlayer is invalid or offline."));
            return;
        }

        Optional<Tournament> optionalTournament = plugin.getTournamentManager().getTournament(input);
        if (optionalTournament.isEmpty()) {
            sender.sendMessage(ColorUtil.color("&cCould not find tournament with that ID"));
            return;
        }

        Tournament tournament = optionalTournament.get();
        tournament.clearParticipant(target.getUniqueId());
        Messages.TOURNAMENT_CLEARED_PLAYER.send(sender, "{TOURNAMENT}", tournament.getIdentifier(), "{PLAYER}", target.getName());
    }

    @SubCommand("list")
    @Permission({"tournaments.admin", "tournaments.command.list"})
    @WrongUsage("&c/tournament list")
    public void listSubCommand(final CommandSender sender) {
        Messages.LIST_TOURNAMENTS.send(sender, "{LIST}", plugin.getTournamentManager().getTournaments().stream().map(Tournament::getIdentifier).collect(Collectors.joining(", ")));
    }

    @SubCommand("end")
    @Permission({"tournaments.admin", "tournaments.command.end"})
    @WrongUsage("&c/tournament end <tournament>")
    @Completion("#tournaments")
    public void endSubCommand(final CommandSender sender, final String input) {
        Optional<Tournament> optionalTournament = plugin.getTournamentManager().getTournament(input);

        if (optionalTournament.isPresent()) {
            Tournament tournament = optionalTournament.get();

            if (tournament.getStatus() == TournamentStatus.ENDED) {
                Messages.ALREADY_STOPPED.send(sender);
            } else {
                tournament.stop();
                tournament.setStatus(TournamentStatus.ENDED);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, tournament::clearParticipants);
                Messages.STOPPED_TOURNAMENT.send(sender, "{TOURNAMENT}", tournament.getIdentifier());
            }
        } else {
            sender.sendMessage(ColorUtil.color("&cCould not find a tournament with that ID."));
        }
    }

    @SubCommand("start")
    @Permission({"tournaments.admin", "tournaments.command.start"})
    @WrongUsage("&c/tournament start <tournament>")
    @Completion("#tournaments")
    public void startSubCommand(final CommandSender sender, final String input) {
        Optional<Tournament> optionalTournament = plugin.getTournamentManager().getTournament(input);

        if (optionalTournament.isPresent()) {
            Tournament tournament = optionalTournament.get();

            if (tournament.getStatus() == TournamentStatus.ACTIVE) {
                Messages.ALREADY_STARTED.send(sender);
            } else {
                tournament.start(false);
                tournament.setStatus(TournamentStatus.ACTIVE);
                Messages.STARTED_TOURNAMENT.send(sender, "{TOURNAMENT}", tournament.getIdentifier());
            }
        } else {
            sender.sendMessage(ColorUtil.color("&cCould not find a tournament with that ID."));
        }
    }


    @SubCommand("forcejoin")
    @Permission({"tournaments.admin", "tournaments.command.forcejoin"})
    @WrongUsage("&c/tournament forcejoin <player/all> <tournament>")
    @Completion({"#players", "#tournaments"})
    public void forceJoinCommand(final CommandSender sender, final String targetInput, final String tournamentInput) {

        Optional<Tournament> optionalTournament = plugin.getTournamentManager().getTournament(tournamentInput);

        // Check if the tournament exists
        if (optionalTournament.isEmpty()) {
            sender.sendMessage(ColorUtil.color("&cCould not find tournament with that ID"));
            return;
        }

        Tournament tournament = optionalTournament.get();

        // Case 1: Force join all players
        if (targetInput.equalsIgnoreCase("all")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tournament.addParticipant(player.getUniqueId(), 0, true);
                Messages.FORCE_JOIN_PLAYER.send(sender, "{PLAYER}", player.getName(), "{TOURNAMENT}", tournament.getIdentifier());
            }
            Messages.FORCE_JOIN_ALL.send(sender, "{TOURNAMENT}", tournament.getIdentifier());
            return;
        }

        // Case 2: Force join a specific player
        Player targetPlayer = Bukkit.getPlayer(targetInput);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(ColorUtil.color("&cThe player is invalid or offline."));
            return;
        }

        if (tournament.getParticipants().containsKey(targetPlayer.getUniqueId())) {
            sender.sendMessage(ColorUtil.color("&cThis player is already in this tournament."));
            return;
        }

        // Add the player to the tournament
        tournament.addParticipant(targetPlayer.getUniqueId(), 0, true);
        Messages.FORCE_JOIN_PLAYER.send(sender, "{PLAYER}", targetPlayer.getName(), "{TOURNAMENT}", tournament.getIdentifier());
    }

    @SubCommand("randomstart")
    @Permission({"tournaments.admin", "tournaments.command.randomstart"})
    @WrongUsage("&c/tournament randomstart")
    @Completion({"#players", "#tournaments"})
    public void randomStartCommand(final CommandSender sender)
    {
        //Start a Random Event from all the configured tournaments
        TournamentManager tournamentManager = plugin.getTournamentManager();
        Map<Tournament, Map<String, FileConfiguration>> allTournamentsMap = tournamentManager.getAllTournaments();
        Iterator<Tournament> iterator = allTournamentsMap.keySet().iterator();
        ArrayList<Tournament> tournaments = new ArrayList<>();
        while (iterator.hasNext())
        {
            Tournament tournament = iterator.next();
            if(tournament.getTimeline().equals(Timeline.RANDOM))
            {
                tournaments.add(tournament);
            }
        }

        Random rand = new Random();
        Tournament randomSelectedTournament = tournaments.get(rand.nextInt(tournaments.size()));
        Map<String, FileConfiguration> randomSelectedTournamentData = allTournamentsMap.get(randomSelectedTournament);
        String identifier = (String) randomSelectedTournamentData.keySet().toArray()[0];
        FileConfiguration config = (FileConfiguration) randomSelectedTournamentData.values().toArray()[0];

        TournamentBuilder tournamentBuilder = tournamentManager.getTournamentBuilder(identifier, config);
        Tournament tournament = tournamentBuilder.build();
        XLObjective objective = tournament.getObjective();
        Logger logger = plugin.getLogger();

        if (!objective.loadTournament(tournament, config)) {
            logger.severe("The objective (\" + obj + \") in file \" + identifier + \" did not load correctly. Skipping..");
            return;
        }

        tournamentManager.enableTournament(identifier, config, true);
    }
}


