package me.imprial.messageredirect;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.Chat;

import java.util.HashMap;
import java.util.Map;

public final class MessageRedirect extends Plugin implements Listener {
    ///creates the hashmap with all the player data
    private Map<ProxiedPlayer, ProxiedPlayer> messageRedirects = new HashMap<>();

    @Override
    public void onEnable() {
        //register the command and listener
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new MessageRedirectCommand());

        //tell console its running
        getProxy().getConsole().sendMessage("Starting message redirect plugin!");
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer sender = (ProxiedPlayer) event.getSender(); //will grab the name of whoever sent the chat message
        ProxiedPlayer receiver = messageRedirects.get(sender); //will go into the hashmap and grab the player who is attached to the sender

        if (receiver != null && !event.isCommand()) { //will check if you are currently chatting with someone and if it is a command, and then cancels your chat message
            //check if that player is still online
            if(receiver.isConnected()){
                event.setCancelled(true); //stop your message from being sent
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "To " + ChatColor.AQUA + receiver.getDisplayName() + ChatColor.WHITE + ": " + event.getMessage()); //give player feedback on if they sent a message
                receiver.sendMessage( ChatColor.LIGHT_PURPLE + "From " + ChatColor.AQUA + sender.getDisplayName() + ChatColor.WHITE + ": " + event.getMessage()); //send the message to the player
            } else{
                event.setCancelled(true);
                sender.sendMessage(ChatColor.RED + "This player is not online!");//tell player they are no longer online
                messageRedirects.remove(sender);

            }
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        messageRedirects.remove(event.getPlayer()); //removes the player from the hashmap
    }

    //private class that handles the

    private class MessageRedirectCommand extends net.md_5.bungee.api.plugin.Command {

        MessageRedirectCommand() {
            super("messageredirect", "messageredirect.use");
        }

        //event for when you run the message redirect command
        @Override
        public void execute(net.md_5.bungee.api.CommandSender sender, String[] args) {
            if (!(sender instanceof ProxiedPlayer)) { //checks if the sender is not a player
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return;
            }

            if (args.length == 0) { //if you have not provided any arguments it will return the usage
                sender.sendMessage(ChatColor.RED + "Usage: /messageredirect <player> to redirect messages or /messageredirect to stop redirection.");
                return;
            }
            //player = the person who ran the command
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (args[0].equalsIgnoreCase("stop")) {
                // Stop redirection
                messageRedirects.remove(player); //removes the player who ran the command
                sender.sendMessage(ChatColor.GREEN + "Message redirection stopped.");
            } else {
                // Redirect messages to the specified player
                ProxiedPlayer target = getProxy().getPlayer(args[0]); //target is who you want to send messages to

                if (target == null) { //if the player returns null then they are not online
                    sender.sendMessage(ChatColor.RED + "Player not online.");
                    return;
                }
                if(target == player){ //check if the targetted player is the same as the sender
                    player.sendMessage(ChatColor.RED + "You cannot message yourself!");
                    return;
                }
                //if the command has not returned yet, this means that you entered a valid player name that isn't yours and that they are online
                //check if you already are in the hashmap
                if(messageRedirects.get(player) != null) {messageRedirects.remove(player);} //this means that you are already in the hashmap, and need to be removed
                messageRedirects.put(player, target); // adds you to the hash map
                sender.sendMessage(ChatColor.GREEN + "Messages will now be redirected to " + target.getName()); //send player confirmation
            }
        }
    }
}
