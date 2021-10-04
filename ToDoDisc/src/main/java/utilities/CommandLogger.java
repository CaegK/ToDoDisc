package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class CommandLogger {
	File logFile;
	FileWriter logWriter;
	static CommandLogger commandLogger = null;
	
	private CommandLogger() { 
		String[] dateTime = DateTimeUtility.getDateTimeNow("HHmmss", "ddMMyy");
		logFile = new File("./log_" + dateTime[0] + "_" + dateTime[1]);
		try {
			logFile.createNewFile();
			System.out.println("Log file: " + logFile.getAbsolutePath());
			logWriter = new FileWriter(logFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static CommandLogger getInstance() {
		if (commandLogger == null) {
			commandLogger = new CommandLogger();
		} 
		return commandLogger;
	}
	
	public void logCommand(SlashCommandEvent e) {
		String[] dateTime = DateTimeUtility.getDateTimeNow();
		try {
			String author = e.getMember().getEffectiveName();
			String command = e.getCommandPath();
			String textChannel = e.getTextChannel().getName();
			String guild = e.getGuild().getName();
			String options = "";
			for(OptionMapping o : e.getOptions()) { 
				options += o.getName() + "|'" + o.getAsString() + "' ";
			}
			logWriter.append("["+dateTime[0]+"," + dateTime[1] + "] " + author + "@" + guild +"," + textChannel +": " + command + " " + options + "\n");
			logWriter.flush();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void logRaw(String author, String msg) { 
		String[] dateTime = DateTimeUtility.getDateTimeNow();
		try { 
			logWriter.append("["+dateTime[0]+"," + dateTime[1] + "] " + author +": " + msg + "\n");
			logWriter.flush();
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}
	
}
