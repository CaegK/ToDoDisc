package view;

import java.awt.Color;
import java.time.LocalDate;
import java.util.List;

import javax.security.auth.login.LoginException;

import application.Controller;
import application.Controller.Reminder;
import application.Controller.Repeater;
import commandBuilder.CommandBuilder;
import dialog.ErrorDialog;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import utilities.DateTimeUtility;
import utilities.DiscTask;

public class DiscordView {
	Controller controller;
	public JDA jda;
	/*
	 * COLORS
	 */
	final Color DARKBLUE = new Color(0,0,139);
	final Color BROWN = new Color(101,67,33);
	final Color SOFTBLUE = new Color(90, 120, 255);
	
	public DiscordView(String token, Controller controller) {
		this.controller = controller;
		JDABuilder jdaBuilder = JDABuilder.createDefault(token);
		jdaBuilder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
		jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
		try {
			jdaBuilder.addEventListeners(new DiscordListener());
			jda = jdaBuilder.build();
			jda.awaitReady();
		} catch (LoginException e) {
			ErrorDialog.showErrorDialog(null, "Invalid Token", true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean getJdaStatus() { 
		if(jda != null) { 
			if(jda.getStatus() == JDA.Status.CONNECTED) { 
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public class DiscordListener extends ListenerAdapter {
		@Override
		public void onSlashCommand(SlashCommandEvent e) {
			controller.processSlashCommand(e);
		}
		
		@Override
		public void onMessageReceived(MessageReceivedEvent e) { 
			if(e.isFromGuild()) { 
				if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("!tododisc1")) { 
					String appId = e.getJDA().retrieveApplicationInfo().complete().getId();
					int[] res = controller.setupCommand(appId, e.getGuild().getId());
					
					setupMsg(e.getTextChannel(), res);
					e.getMessage().delete().queue();
				}
			}
		}
	}
	
	/*
	 * Response functions
	 */
	public void setupMsg(TextChannel channel, int[] res) {
		Field[] fields = new Field[res.length];
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.ORANGE);
		eb.setTitle("Slash Commands");
		
		for(int i=0; i<res.length; i++) { 
			if(res[i] == 201) { 
				fields[i] = new Field("/" + CommandBuilder.COMMANDS[i], "Successfully setup", false);
			} else if (res[i] == 200) { 
				fields[i] = new Field("/" + CommandBuilder.COMMANDS[i], "Already setup", false);
			} else {
				fields[i] = new Field("/" + CommandBuilder.COMMANDS[i], "Error setting up", false);
			}
			eb.addField(fields[i]);
		}
		channel.sendMessageEmbeds(eb.build()).queue();
	}
	
	/*
	 * TASK CATEGORY
	 */
	//task category already exists 
	public void taskCatAlreadyExists(InteractionHook hook, String taskCatName) {
		hook.editOriginal("This task category already exists in the discord server, to avoid conflicts please delete the category first").queue();
	}
	
	public void taskCatAlreadyExistsSql(InteractionHook hook, String taskCatName) {
		hook.editOriginal("This task category already exists in the database, to avoid conflicts please delete the category first").queue();
	}
	
	//task category created
	public void taskCatCreated(InteractionHook hook, String taskCatName) {
		hook.editOriginal("task category, " + taskCatName + " created").queue();
	}
	
	//task category deleted 
	public void taskCatDeleted(InteractionHook hook) {
		hook.editOriginal("Task category deleted from database").queue();
	}
	
	//task category dosen't exists
	public void taskCatDoesntExist(InteractionHook hook, String taskCatName) {
		hook.editOriginal("No such tasks exists").queue();
	}
	
	public void taskCatCreateError(InteractionHook hook) {
		hook.editOriginal("Error! Could not talk to database...").queue();
	}
	
	public void taskCatCreatedIntro(TextChannel mainChannel, String author, String avatarUrl, String desc, String name) {
		//Intro msg
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setTitle("Task category `" + name + "` created and is associated with this text channel")
		.setFooter(author, avatarUrl)
		.setDescription("Date: `" + LocalDate.now().toString() + "` \n"
				+"Time: `" + DateTimeUtility.getTimeSecond() + "`\n");
		
		if(!desc.isEmpty()) { 
			eb.appendDescription("Description: " + desc);
		}
		
		mainChannel.sendMessageEmbeds(eb.build()).queue();
	}
	
	/*
	 * TASK
	 */
	public void taskAlreadyExist(InteractionHook hook) { 
		hook.editOriginal("Task with the same name already exists in this task category").queue();
	}
	
	public void taskCreated(InteractionHook hook) {
		hook.editOriginal("Task created").queue();
	}
	
	public void taskDoesntExist(InteractionHook hook) {
		hook.editOriginal("Task dosent exist").queue();
	}
	
	public void taskDeleted(InteractionHook hook) {
		hook.editOriginal("Task deleted").queue();
	}
	
	public void taskEmpty(InteractionHook hook) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.red);
		eb.setTitle("No tasks found in this category");
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void taskDoesntExistByName(InteractionHook hook, String name) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.red);
		eb.setTitle("The task `"+name+"` does not exist in this category.");
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void taskDoesntExistById(InteractionHook hook, int id) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.red);
		eb.setTitle("The task by the id `"+id+"` does not exist in this category.");
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void taskViewAll(InteractionHook hook, List<DiscTask> tasks) {
		EmbedBuilder eb = new EmbedBuilder();
		for(DiscTask t : tasks) {
			String details = 
					"ID: " + t.getId() + "\n" + 
					"Status: " + t.getStatus() + "\n";
			
			//check for blank fields
			if(!t.getAssigned().isEmpty()) {
				details = details + "assigned to: " + t.getAssigned() + "\n";
			}
			if(!t.getDesc().isEmpty()) {
				details = details + "Description: " + t.getDesc() + "\n";
			}	
			
			details = details + "created by `" + t.getAuthor() + "` on `" + t.getDate() + "` at `" + t.getTime() + "`\n\n";
			
			eb.addField("**" + t.getName() + "**",
					details,
					false);
			eb.setTitle("All Tasks in this category");
			eb.setColor(DARKBLUE);
		}
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void taskViewByName(InteractionHook hook, DiscTask task, String name) { 
		EmbedBuilder eb = new EmbedBuilder();
		
		String details = 
				"ID: " + task.getId() + "\n" + 
				"Status: " + task.getStatus() + "\n";
		
		//check for blank fields
		if(!task.getAssigned().isEmpty()) {
			details = details + "assigned to: " + task.getAssigned() + "\n";
		}
		if(!task.getDesc().isEmpty()) {
			details = details + "Description: " + task.getDesc() + "\n";
		}	
		
		details = details + "created by `" + task.getAuthor() + "` on `" + task.getDate() + "` at `" + task.getTime() + "`\n\n";
		
		eb.addField("**" + task.getName() + "**",
				details,
				false);
		eb.setTitle("Found by name: `"+name+"`");
		eb.setColor(DARKBLUE);
		
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void taskViewById(InteractionHook hook, DiscTask task, int id) { 
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.addField("**" + task.getName() + "**",
				task.format(),
				false);
		eb.setTitle("Found by ID: `"+ id +"`");
		eb.setColor(DARKBLUE);
		
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	/*
	 * REMINDER
	 */
	public void reminderSetEphemeral(InteractionHook hook, Reminder r) {
		EmbedBuilder eb = new EmbedBuilder();
		String remDesc = "Remind on: `" + r.dateTime[0] + "`, `"+r.dateTime[1]+"`" +"\n"+"Remind user: `"+r.userMem.getEffectiveName()+"`\n";
		if(!r.desc.isEmpty()) {
			remDesc += "Reminder Description: `" + r.desc + "`\n";
		}
		
		eb.setColor(BROWN);
		eb.setTitle("Reminder set")
		.setDescription(remDesc)
		.addField(r.task.getName(),r.task.format(), false)
		.setFooter(r.author.getEffectiveName(), r.author.getUser().getEffectiveAvatarUrl());
		
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void reminderSetPublic(InteractionHook hook, Reminder r, TextChannel channel) {
		EmbedBuilder eb = new EmbedBuilder();
		String remDesc = "Remind on: `" + r.dateTime[0] + "`, `"+r.dateTime[1]+"`" +"\n"+"Remind user: `"+r.userMem.getEffectiveName()+"`\n";
		if(!r.desc.isEmpty()) {
			remDesc += "Reminder Description: `" + r.desc + "`\n";
		}
		
		eb.setColor(BROWN);
		eb.setTitle("Reminder set")
		.setDescription(remDesc)
		.addField(r.task.getName(),r.task.format(), false)
		.setFooter(r.author.getEffectiveName(), r.author.getUser().getEffectiveAvatarUrl());
		
		channel.sendMessageEmbeds(eb.build()).queue();
		hook.editOriginal("Reminder set for " + r.userMem.getEffectiveName()).queue();
	}
	
	public void reminderView(InteractionHook hook, List<Reminder> reminders) { 
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setColor(BROWN);
		eb.setTitle("Your reminders");
		if(reminders.isEmpty()) { 
			eb.setDescription("No reminders set");
		} else {
			for(Reminder r: reminders) { 
				String des = "Task ID: `" + r.task.getId() + "` \n" 
						+ "Remind at: `" + r.dateTime[0] + ", " + r.dateTime[1] + "` \n" 
						+ "Remind user: `" + r.userMem.getEffectiveName() + "`\n" 
						+ "Reminder ID: `" + r.id + "`\n"
						+ "Set by: `" + r.author.getEffectiveName() + "` \n";
				if(!r.desc.isEmpty()) {
					des+= "Reminder Description: `" + r.desc + "`\n";
				}
				
				eb.addField("`"+r.task.getName()+"`", des, false);
			}
		}
		
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void reminderDirect(Reminder r) { 
		EmbedBuilder eb = new EmbedBuilder();
		String remDesc = r.task.formatGuild();
		if(!r.desc.isEmpty()) {
			remDesc += "Reminder Description: `" + r.desc + "` \n";
		}
		eb.setColor(BROWN);
		
		eb.setTitle("Reminder for task `" + r.task.getName() + "` \n")
		.setDescription(remDesc)
		.setFooter(r.author.getEffectiveName(), r.author.getUser().getEffectiveAvatarUrl());
		
		r.user.openPrivateChannel()
		.flatMap((channel) -> {
			return channel.sendMessageEmbeds(eb.build());
		})
		.queue();
	}
	
	public void reminderCancelled(InteractionHook hook, Reminder r) { 
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setColor(BROWN);
		eb.setTitle("Reminder cancelled")
		.setFooter(r.author.getEffectiveName(), r.author.getUser().getEffectiveAvatarUrl());
		
		String des = "Task ID: `" + r.task.getId() + "` \n" 
				+ "Remind at: `" + r.dateTime[0] + ", " + r.dateTime[1] + "` \n" 
				+ "Remind user: `" + r.userMem.getEffectiveName() + "`\n" 
				+ "Reminder ID: `" + r.id + "`\n"
				+ "Set by: `" + r.author.getEffectiveName() + "` \n";
		
		eb.addField("`"+r.task.getName()+"`", des, false);
		
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void reminderDosentExist(InteractionHook hook, int id) { 
		hook.editOriginalEmbeds(new EmbedBuilder()
				.setColor(Color.RED)
				.setDescription("No reminder of the ID: `" +id +"` exists")
				.build()).queue();
	}
	
	/*
	 * Repeat
	 */
	public void repeatSet(InteractionHook hook, Repeater r) { 
		EmbedBuilder eb = new EmbedBuilder();
		String remDesc = "";
		
		if(!r.desc.isEmpty()) {
			remDesc += "Repeater Description: `" + r.desc + "` \n";
		}
		remDesc += "Repeats every: `" + r.periodString + "`\n"
				+ "Next repeat: `" + r.nextRepeat[0] + " at " + r.nextRepeat[1] + "`\n"
				+ "Repeater ID: `" + r.id + "` \n";
		eb.setColor(SOFTBLUE);
		
		eb.setTitle("Repeater **set** for task `" + r.task.getName() + "` \n")
		.setDescription(remDesc)
		.setFooter(r.member.getEffectiveName(), r.member.getUser().getEffectiveAvatarUrl());
		
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void repeatDirect(Repeater r) { 
		EmbedBuilder eb = new EmbedBuilder();
		String remDesc = r.task.formatGuild();
		
		if(!r.desc.isEmpty()) {
			remDesc += "Repeater Description: `" + r.desc + "` \n";
		}
		remDesc += "Repeats every: `" + r.periodString + "`\n"
				+ "Next repeat: `" + r.nextRepeat[0] + " at " + r.nextRepeat[1] + "`\n"
				+ "Repeater ID: `" + r.id + "` \n";
		eb.setColor(SOFTBLUE);
		
		eb.setTitle("Repeater for task `" + r.task.getName() + "` \n")
		.setDescription(remDesc)
		.setFooter(r.member.getEffectiveName(), r.member.getUser().getEffectiveAvatarUrl());
		
		r.user.openPrivateChannel()
		.flatMap((channel) -> {
			return channel.sendMessageEmbeds(eb.build());
		})
		.queue();
	}
	
	public void repeaterView(InteractionHook hook, List<Repeater> repeaters) { 
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setColor(SOFTBLUE);
		eb.setTitle("Your repeats");
		if(repeaters.isEmpty()) { 
			eb.setDescription("No repeats set");
		} else {
			for(Repeater r: repeaters) { 
				String des = "Task ID: `" + r.task.getId() + "` \n" 
						+ "Repeats every: `" + r.periodString + "`\n"
						+ "Next repeat: `" + r.nextRepeat[0] + " at " + r.nextRepeat[1] + "`\n"
						+ "Repeater ID: `" + r.id + "`\n";
				if(!r.desc.isEmpty()) {
					des+= "Repeater Description: `" + r.desc + "`\n";
				}
				
				eb.addField("`"+r.task.getName()+"`", des, false);
			}
		}
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void repeaterCancelled(InteractionHook hook, Repeater r) { 
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setColor(SOFTBLUE);
		eb.setTitle("Repeater cancelled")
		.setFooter(r.member.getEffectiveName(), r.user.getEffectiveAvatarUrl());
		
		String des = "Task ID: `" + r.task.getId() + "` \n" 
				+ "Repeats every: `" + r.periodString + "`\n"
				+ "Next repeat: `" + r.nextRepeat[0] + " at " + r.nextRepeat[1] + "`\n"
				+ "Repeater ID: `" + r.id + "`\n";
		
		eb.addField("`"+r.task.getName()+"`", des, false);
		
		hook.editOriginalEmbeds(eb.build()).queue();
	}
	
	public void repeaterDosentExist(InteractionHook hook, int id) { 
		hook.editOriginalEmbeds(new EmbedBuilder()
				.setColor(Color.RED)
				.setDescription("No repeater of the ID: `" +id +"` exists")
				.build()).queue();
	}
	
	/*
	 * STATUS
	 */
	public void statusUpdated(InteractionHook hook, DiscTask task) { 
		hook.editOriginal(task.getName() + "'s status updated successfully").queue();
	}
	
	/*
	 * LOG
	 */
	
	public void logTaskCreated(TextChannel channel, String author, String avatar, DiscTask task) { 
		EmbedBuilder eb = new EmbedBuilder();
		System.out.println("Author " + author + ", avatar " + avatar);
		
		eb.setTitle("New task created, `"+task.getName()+"`")
		.setDescription(task.format())
		.setColor(Color.cyan)
		.setFooter(author, avatar);
		
		channel.sendMessageEmbeds(eb.build()).queue();
	}
	
	public void logTaskDeleted(TextChannel channel, String author, String avatar, DiscTask task) { 
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setTitle("Task deleted, `"+task.getName()+"`")
		.setDescription(task.format())
		.setColor(Color.ORANGE)
		.setFooter(author, avatar);
		
		channel.sendMessageEmbeds(eb.build()).queue();
	}
	
	public void logTaskCatDeleted(TextChannel channel, String author, String avatar) {
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setTitle("Task category deleted")
		.setDescription("This channel is no longer associated with a task category")
		.setColor(Color.RED)
		.setFooter(author, avatar);
		
		channel.sendMessageEmbeds(eb.build()).queue();
	}
	
	public void logTaskStatusUpdate(TextChannel channel, String author, String avatar, DiscTask task) { 
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setColor(Color.PINK);
		
		eb.setTitle("Updated task `" + task.getName() + "`'s status")
		.setDescription(task.format())
		.setFooter(author, avatar);
		
		channel.sendMessageEmbeds(eb.build()).queue();
	}
	
	/*
	 * ERRORS
	 */
	
	public void sqlCreationError(InteractionHook hook) { 
		hook.sendMessageEmbeds(new EmbedBuilder().setTitle("Error inserting data in SQL database").setColor(Color.red).build()).queue();
	}
	
	public void notAValidNumber(InteractionHook hook, String id) { 
		hook.sendMessageEmbeds(new EmbedBuilder().setTitle("Not a valid ID, `"+id+ "`. Must be numbers only.").setColor(Color.red).build()).queue();
	}
	
	public void reminderTooLong(InteractionHook hook) { 
		hook.sendMessageEmbeds(new EmbedBuilder().setTitle("Maximum reminder duration is 30 days.").setColor(Color.red).build()).queue();
	}
	
	public void reminderNoDuration(InteractionHook hook) { 
		hook.sendMessageEmbeds(new EmbedBuilder().setTitle("Please set a duration for the reminder.").setColor(Color.red).build()).queue();
	}
	
	public void repeatInvalidTime(InteractionHook hook) { 
		hook.sendMessageEmbeds(new EmbedBuilder().setTitle("Please set a valid target time.").setColor(Color.red).build()).queue();
	}
	
	public void notAValidChannel(InteractionHook hook) {
		hook.sendMessageEmbeds(new EmbedBuilder().setTitle("This not a valid channel.").setColor(Color.red).build()).queue();
	}
	
	
	/*
	 * DEBUG
	 */
	public void debugPresentTask(InteractionHook hook, DiscTask task) { 
		hook.sendMessageEmbeds(new EmbedBuilder().setTitle(task.getName()).setDescription(task.format()).build()).queue();
	}
	
}
