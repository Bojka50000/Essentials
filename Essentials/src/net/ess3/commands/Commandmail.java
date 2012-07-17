package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.utils.Util;
import net.ess3.api.IUser;
import net.ess3.api.server.CommandSender;
import net.ess3.permissions.Permissions;
import java.util.List;
import net.ess3.api.server.Player;



public class Commandmail extends EssentialsCommand
{
	//TODO: Tidy this up
	@Override
	public void run(final IUser user, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length >= 1 && "read".equalsIgnoreCase(args[0]))
		{
			final List<String> mail = user.getData().getMails();
			if (mail == null || mail.isEmpty())
			{
				user.sendMessage(_("noMail"));
				throw new NoChargeException();
			}
			for (String messages : mail)
			{
				user.sendMessage(messages);
			}
			user.sendMessage(_("mailClear"));
			return;
		}
		if (args.length >= 3 && "send".equalsIgnoreCase(args[0]))
		{
			if (!Permissions.MAIL_SEND.isAuthorized(user))
			{
				throw new Exception(_("noPerm", "essentials.mail.send"));
			}

			Player player = server.getPlayer(args[1]);
			IUser u;
			if (player != null)
			{
				u = ess.getUser(player);
			}
			else
			{
				u = ess.getUser(args[1]);
			}
			if (u == null)
			{
				throw new Exception(_("playerNeverOnServer", args[1]));
			}
			if (!u.isIgnoringPlayer(user))
			{
				final String mail = Util.sanitizeString(Util.stripFormat(getFinalArg(args, 2)));
				u.addMail(user.getName() + ": " + mail);
			}
			user.sendMessage(_("mailSent"));
			return;
		}
		if (args.length > 1 && "sendall".equalsIgnoreCase(args[0]))
		{
			if (!Permissions.MAIL_SENDALL.isAuthorized(user))
			{
				throw new Exception(_("noPerm", "essentials.mail.sendall"));
			}
			ess.getPlugin().scheduleAsyncDelayedTask(new SendAll(user.getName() + ": " + Util.stripColor(getFinalArg(args, 1))));
			user.sendMessage(_("mailSent"));
			return;
		}
		if (args.length >= 1 && "clear".equalsIgnoreCase(args[0]))
		{
			user.acquireWriteLock();
			user.getData().setMails(null);
			user.sendMessage(_("mailCleared"));
			return;
		}
		throw new NotEnoughArgumentsException();
	}

	@Override
	protected void run(final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length >= 1 && "read".equalsIgnoreCase(args[0]))
		{
			throw new Exception(_("onlyPlayers", commandName + " read"));
		}
		else if (args.length >= 1 && "clear".equalsIgnoreCase(args[0]))
		{
			throw new Exception(_("onlyPlayers", commandName + " clear"));
		}
		else if (args.length >= 3 && "send".equalsIgnoreCase(args[0]))
		{
			Player player = server.getPlayer(args[1]);
			IUser u;
			if (player != null)
			{
				u = ess.getUser(player);
			}
			else
			{
				u = ess.getUser(args[1]);
			}
			if (u == null)
			{
				throw new Exception(_("playerNeverOnServer", args[1]));
			}
			u.addMail("Server: " + getFinalArg(args, 2));
			sender.sendMessage(_("mailSent"));
			return;
		}
		else if (args.length >= 1 && "sendall".equalsIgnoreCase(args[0]))
		{
			ess.getPlugin().scheduleAsyncDelayedTask(new SendAll("Server: " + getFinalArg(args, 2)));
		}
		else if (args.length >= 2)
		{
			//allow sending from console without "send" argument, since it's the only thing the console can do
			Player player = server.getPlayer(args[0]);
			IUser u;
			if (player != null)
			{
				u = ess.getUser(player);
			}
			else
			{
				u = ess.getUser(args[0]);
			}
			if (u == null)
			{
				throw new Exception(_("playerNeverOnServer", args[0]));
			}
			u.addMail("Server: " + getFinalArg(args, 1));
			sender.sendMessage(_("mailSent"));
			return;
		}
		throw new NotEnoughArgumentsException();
	}


	private class SendAll implements Runnable
	{
		String message;

		public SendAll(String message)
		{
			this.message = message;
		}

		@Override
		public void run()
		{
			for (String username : ess.getUserMap().getAllUniqueUsers())
			{
				IUser user = ess.getUserMap().getUser(username);
				if (user != null)
				{
					user.addMail(message);
				}
			}
		}
	}
}