/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.zcraft.zlib.components.commands;

import fr.zcraft.zlib.components.commands.CommandException.Reason;
import fr.zcraft.zlib.core.ZLib;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract public class Command 
{   
    protected final CommandGroup commandGroup;
    protected final String commandName;
    protected final String usageParameters;
    protected final String commandDescription;
    protected final String[] aliases;
    
    protected CommandSender sender;
    protected String[] args;
    
    abstract protected void run() throws CommandException;
    
    Command(CommandGroup commandGroup)
    {
        this.commandGroup = commandGroup;
        
        CommandInfo commandInfo = this.getClass().getAnnotation(CommandInfo.class);
        if(commandInfo == null) 
            throw new IllegalArgumentException("Command has no CommandInfo annotation");
        
        commandName = commandInfo.name().toLowerCase();
        usageParameters = commandInfo.usageParameters();
        commandDescription = commandGroup.getDescription(commandName);
        aliases = commandInfo.aliases();
    }
    
    public boolean canExecute(CommandSender sender)
    {
        String permissionPrefix = ZLib.getPlugin().getName().toLowerCase() + ".";
        String globalPermission = Commands.getGlobalPermission();
        
        if(globalPermission != null)
            if(sender.hasPermission(permissionPrefix + globalPermission))
                return true;
        
        return sender.hasPermission(permissionPrefix + commandGroup.getUsualName());
    }
    
    protected List<String> complete() throws CommandException
    {
        return null;
    }
    
    public void execute(CommandSender sender, String[] args)
    {
        this.sender = sender; this.args = args;
        try
        {
            if(!canExecute(sender))
                throw new CommandException(this, Reason.SENDER_NOT_AUTHORIZED);
            run();
        }
        catch(CommandException ex)
        {
            warning(ex.getReasonString());
        }
        this.sender = null; this.args = null;
    }
    
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        List<String> result = null;
        this.sender = sender; this.args = args;
        try
        {
            if(canExecute(sender))
                result = complete();
        }
        catch(CommandException ex){}
        
        this.sender = null; this.args = null;
        if(result == null) result = new ArrayList<String>();
        return result;
    }
    
    
    public String getUsageString()
    {
        return "/" + commandGroup.getUsualName() + " " + commandName + " " + usageParameters;
    }
    
    public String getName()
    {
        return commandName;
    }
    
    CommandGroup getCommandGroup()
    {
        return commandGroup;
    }
    
    public String[] getAliases()
    {
        return aliases;
    }
    
    public boolean matches(String name)
    {
        if(commandName.equals(name.toLowerCase())) return true;
        
        for(String alias : aliases)
        {
            if(alias.equals(name)) return true;
        }
        
        return false;
    }
    
    
    ///////////// Common methods for commands /////////////
    
    protected void throwInvalidArgument(String reason) throws CommandException
    {
        throw new CommandException(this, Reason.INVALID_PARAMETERS, reason);
    }
        
    protected Player playerSender() throws CommandException
    {
        if(!(sender instanceof Player)) 
            throw new CommandException(this, Reason.COMMANDSENDER_EXPECTED_PLAYER);
        return (Player)sender;
    }
        
    ///////////// Methods for command execution /////////////
    
    static protected void info(CommandSender sender, String message)
    {
        sender.sendMessage("§7" + message);
    }
    
    protected void info(String message)
    {
        info(sender, message);
    }
    
    static protected void warning(CommandSender sender, String message)
    {
        sender.sendMessage("§c" + message);
    }
    
    protected void warning(String message)
    {
        info(sender, message);
    }
    
    protected void error(String message) throws CommandException
    {
        throw new CommandException(this, Reason.COMMAND_ERROR, message);
    }
    
    protected void tellRaw(String rawMessage) throws CommandException
    {
        Player player = playerSender();
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), 
                "tellraw " + player.getName() + " " + rawMessage);
    }
    
    ///////////// Methods for autocompletion /////////////
    
    protected List<String> getMatchingSubset(String prefix, String... list)
    {
        return getMatchingSubset(Arrays.asList(list), prefix);
    }
    
    protected List<String> getMatchingSubset(Iterable<? extends String> list, String prefix)
    {
        List<String> matches = new ArrayList<String>();
        
        for(String item : list)
        {
            if(item.startsWith(prefix)) matches.add(item);
        }
        
        return matches;
    }
    
    protected List<String> getMatchingPlayerNames(String prefix)
    {
        return getMatchingPlayerNames(Bukkit.getOnlinePlayers(), prefix);
    }
    
    protected List<String> getMatchingPlayerNames(Iterable<? extends Player> players, String prefix)
    {
        List<String> matches = new ArrayList<String>();
        
        for(Player player : players)
        {
            if(player.getName().startsWith(prefix)) matches.add(player.getName());
        }
        
        return matches;
    }

}
