package application;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import commandBuilder.CommandBuilder;
import database.SQLModel;
import dialog.ErrorDialog;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import utilities.CommandLogger;
import utilities.DateTimeUtility;
import utilities.DiscTask;
import view.DiscordView;
import view.InitGUI;

public class Controller extends Application{
	final long MAX_REMINDER_DURATION = 2592000L; //30 days in seconds
	final long DAYS_TO_SEC = 86400L;
	final long HOURS_TO_SEC = 3600L;
	final long MINUTES_TO_SEC = 60L;
	final long SECONDS_TO_SEC = 1L;
	
	/*
	*GUI objects
	*/
	InitGUI initGui;
	
	SQLModel sqlModel;
	DiscordView discordView;
	ExecutorService slashCommandExecutor = Executors.newFixedThreadPool(10);
	
	/*
	 * REMINDER OBJECTS
	 */
	ScheduledThreadPoolExecutor reminderExecutor = new ScheduledThreadPoolExecutor(10);
	HashMap<Integer, Reminder> reminderMap = new HashMap<Integer, Reminder>();
	int reminderIdx = 0;
	
	/*
	 * REPEATER OBJECTS
	 */
	ScheduledThreadPoolExecutor repeaterExecutor = new ScheduledThreadPoolExecutor(10);
	HashMap<Integer, Repeater> repeaterMap = new HashMap<Integer, Repeater>();
	int repeaterIdx = 0;
	
	/*
	 * LOGGING INSTANCE
	 */
	CommandLogger logger;
	
	/*
	 * InitGUI logic objects
	 */
	String botToken;
	EventHandler<ActionEvent> connectToBot = (e) ->{
		initGui.disableConnect();
		botToken = initGui.botTokenField.getText();
		//check for empty text in textfield
		if(!botToken.trim().isEmpty()) {
			discordView = new DiscordView(botToken, this);
			if (!discordView.getJdaStatus()) {
				ErrorDialog.showErrorDialog(null, "Could not connect to bot", true);
				initGui.enableConnect();
				return;
			}
		} else {
			botToken = "";
			ErrorDialog.showErrorDialog(null, "Not a valid token!", true);
		}
	};
	Runnable updateStatusRunnable = () ->{
		if(discordView != null) {
			if(discordView.getJdaStatus()) { 
				initGui.showJdaOnline();
			} else {
				initGui.showJdaOffline();
			}
		} else {
			initGui.showJdaOffline();
		}
	};
	Runnable updateDbConnRunnable = () -> {
		if(sqlModel != null) { 
			if(sqlModel.isConnected()) { 
				initGui.showDbOnline();
			} else {
				initGui.showDbOffline();
				sqlModel.restartConnection();
			}
		} else {
			initGui.showDbOffline();
		}
	};
	ScheduledExecutorService statusTimer;
	
	public void entryPoint(String[] args) {
		launch(args);
	}
	
	@Override
	public void init() {
		//set policy for reminder executor
		reminderExecutor.setRemoveOnCancelPolicy(true);
		repeaterExecutor.setRemoveOnCancelPolicy(true);
		
		//setup sql server
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		sqlModel = new SQLModel();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		//initGUI setup
		initGui = new InitGUI();
		initGui.jdaConnectBtn.setOnAction(connectToBot);
		initGui.show();
		//run status update thread
		Thread updateStatusThread = new Thread(updateStatusRunnable);
		Thread updateDbConnThread = new Thread(updateDbConnRunnable);
		updateDbConnThread.setDaemon(true);
		updateStatusThread.setDaemon(true);
		statusTimer = Executors.newScheduledThreadPool(2);
		statusTimer.scheduleAtFixedRate(updateDbConnThread, 1, 3, TimeUnit.SECONDS);
		statusTimer.scheduleAtFixedRate(updateStatusThread, 1, 1, TimeUnit.SECONDS);
		
		initGui.frame.setOnClose(()->{
			logger.logRaw("System", "Process ended by User");
			sqlModel.close();
			System.exit(0);
		});
		
		logger = CommandLogger.getInstance();
		logger.logRaw("System", "Process started");
	}
	
	public void processSlashCommand(SlashCommandEvent e) {
		slashCommandExecutor.submit(new SlashCommandHandler(e));
	}
	
	public int[] setupCommand(String appId, String guildId) { 
		CommandBuilder c = new CommandBuilder(initGui.botTokenField.getText().trim(), appId, guildId);
		return c.updateAllCommands();
	}
	
	public class Reminder implements Runnable {
		public DiscTask task;
		public User user;
		public Member author;
		public Member userMem;
		public int id;
		public String[] dateTime;
		@SuppressWarnings("rawtypes")
		Future future;
		public String desc = "";

		public Reminder(DiscTask task, User user, Member author, Member userMem, Integer id, String[] dateTime) { 
			this.task = task;
			this.user = user;
			this.author = author;
			this.userMem = userMem;
			this.id = id;
			this.dateTime = dateTime;
		}
		
		@Override
		public void run() {
			reminderMap.remove(id);
			discordView.reminderDirect(this);
		}
		
		@SuppressWarnings("rawtypes")
		public Future getFuture() {
			return future;
		}

		@SuppressWarnings("rawtypes")
		public void setFuture(Future future) {
			this.future = future;
		}
		
		public void setDescription(String desc) { 
			this.desc = desc;
		}
	}
	
	public class Repeater implements Runnable { 
		public DiscTask task = null;
		public User user = null;
		public Member member = null;
		public Duration period;
		public String periodString;
		public String[] nextRepeat;
		public String desc = "";
		public int id;
		@SuppressWarnings("rawtypes")
		public Future future;
		
		public Repeater (DiscTask task, User user, Member member, Duration period, int id) { 
			this.task = task; 
			this.user = user;
			this.member = member;
			this.period = period;
			this.id = id;
			periodString = DateTimeUtility.formatDuration(period);
		}
		
		public void setDesc(String desc) { 
			this.desc = desc;
		}
		
		public void setNextRepeat(String[] nextRepeat) {
			this.nextRepeat = nextRepeat;
		}
		
		@SuppressWarnings("rawtypes")
		public void setFuture(Future future) { 
			this.future = future;
		}
		
		@Override
		public void run() { 
			nextRepeat = DateTimeUtility.getDateTimeOffset(period);
			discordView.repeatDirect(this);
		}
	}
	
	public class SlashCommandHandler implements Runnable {
		SlashCommandEvent e;
		public SlashCommandHandler(SlashCommandEvent e) {
			logger.logCommand(e);
			this.e = e;
		}
		
		@Override
		public void run() {
			if (e.getName().equals("taskcategory")) {
				List<OptionMapping> options = e.getOptions();
				//TODO permission checks 
				// taskcategory create [name]
				if (e.getSubcommandName().equals("create")) { 
					commandTaskCategoryCreate(options);
				}
				// taskcategory delete [name]
				else if (e.getSubcommandName().equals("delete")) {
					commandTaskCategoryDelete(options);
				} 
				// taskcategory test 
				else if (e.getSubcommandName().equals("test")) {
					e.deferReply(true).queue();
				}
			} 
			// tasks
			else if (e.getName().equals("task")) {
				List<OptionMapping> options = e.getOptions();
				//TODO permission checks 
				// task create [name] [description]
				if (e.getSubcommandName().equals("create")) { 
					commandTaskCreate(options);
				} 
				// task delete [name/id]
				else if (e.getSubcommandName().equals("delete")) {
					String idType = options.get(0).getAsString();
					// task delete [name]
					if(idType.equals("name")) {
						commandTaskDeleteByName(options);
					} 
					// task delete [id]
					else if(idType.equals("id")) {
						commandTaskDeleteById(options);
					}
				}
			}
			// view
			else if (e.getName().equals("view")) {
				List<OptionMapping> options = e.getOptions();
				OptionMapping firstOpt = options.get(0);
				
				if(firstOpt.getAsString().equals("id")) {
					commandViewById(options);
				} else if (firstOpt.getAsString().equals("name")) {
					commandViewByName(options);
				} else if (firstOpt.getAsString().equals("all")) { 
					commandViewAll();
				} else if (firstOpt.getAsString().equals("status")) {
					commandViewByStatus(options);
				}
			}
			// reminder
			else if (e.getName().equals("reminder")) {
				List<OptionMapping> options = e.getOptions();
				
				//reminder set
				if(e.getSubcommandName().equals("set")) { 
					commandReminderSet(options);
				}
				//reminder view 
				else if (e.getSubcommandName().equals("view")) {
					commandReminderView(options);
				}
				//reminder delete
				else if (e.getSubcommandName().equals("delete")) {
					commandReminderDelete(options);
				}
			}
			// repeat
			else if (e.getName().equals("repeat")) {
				List<OptionMapping> options = e.getOptions();
				
				//reminder set
				if(e.getSubcommandName().equals("set")) { 
					commandRepeatSet(options);
				} 
				// reminder delete
				else if(e.getSubcommandName().equals("delete")) { 
					commandRepeatDelete(options);
				} 
				// reminder view
				else if(e.getSubcommandName().equals("view")) { 
					commandRepeatView(options);
				} 
			}
			// status
			else if (e.getName().equals("status")) {
				List<OptionMapping> options = e.getOptions();
				commandStatus(options);
			}
		}
		
		public void commandTaskCategoryCreate(List<OptionMapping> options) {
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			String name = options.get(0).getAsString();
			String desc = "";
			Guild guild = e.getGuild(); //get currentguild
			
			//define description if any
			if(options.size()>1) {
				desc = options.get(1).getAsString();
			}
			//if task category exists
			if (sqlModel.doesTaskCategoryExistName(guild.getId(), name)) {
				discordView.taskCatAlreadyExistsSql(hook, name);
			}
			//if task category dosent exist then proceed to make the task category
			else {
				guild.createTextChannel(name+"-task-category", null).complete();
				String tasksId = null;
				//waits for Discord to update their servers (10 seconds)
				int tolerance = 0;
				while((tasksId == null) && tolerance <= 10) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					List<TextChannel> textChannels = guild.getTextChannels();
					for(TextChannel t : textChannels) {
						if (t.getName().equals(name+"-task-category")) {
							tasksId = t.getId();
						}
					}
					tolerance++;
				}
				if(tasksId != null ) {
					if (sqlModel.newTaskCategory(guild.getId(), name, desc, tasksId)) {
						discordView.taskCatCreated(hook, name);
						discordView.taskCatCreatedIntro(guild.getTextChannelById(tasksId), e.getMember().getEffectiveName(), e.getUser().getEffectiveAvatarUrl(), desc, name);
					}
				} else {
					discordView.taskCatCreateError(hook);
				}
			}
		}
		
		public void commandTaskCategoryDelete(List<OptionMapping> options) {
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			Guild guild = e.getGuild(); //get currentguild
			TextChannel channel = e.getTextChannel();

			if (!sqlModel.doesTaskCategoryExistId(guild.getId(), channel.getId())) {
				discordView.taskCatDoesntExist(hook, channel.getId());
			}
			else {
				//TODO delete from sql here and request confirmation w/ buttons omg
				sqlModel.removeTaskCategory(guild.getId(), channel.getId());
				discordView.taskCatDeleted(hook);
				discordView.logTaskCatDeleted(channel, e.getMember().getEffectiveName(), e.getUser().getEffectiveAvatarUrl());
			}
		}
		
		public void commandTaskCreate(List<OptionMapping> options) {
			/*
			 * 1. Check if text channel is a task-cat channel
			 * 2. Check if task already exist in that task-cat
			 * 3. Insert task into sql 
			 * 4. Update discord hook
			 */
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			String name = options.get(0).getAsString();
			String description = "";
			String channelId;
			String guildId;
			DiscTask task;
			
			//if the option > 1, then the user put a description
			if(options.size()>1) { 
				description = options.get(1).getAsString();
			}
			//checking for valid textchannel
			channelId = e.getTextChannel().getId();
			guildId = e.getGuild().getId();
			if(sqlModel.checkValidChannel(guildId, channelId)) {
				if(!sqlModel.doesTaskExistByName(guildId, channelId, name)) {
					task = sqlModel.createTask(guildId, channelId, name, description, e.getUser().getName(), e.getUser().getId());
					if(task != null) {
						task.retrieveNames(e.getGuild());
						discordView.taskCreated(hook);
						discordView.logTaskCreated(e.getTextChannel(), e.getMember().getEffectiveName(), e.getUser().getEffectiveAvatarUrl(), task);
					} else {
						discordView.sqlCreationError(hook);
					}
				} else {
					discordView.taskAlreadyExist(hook);
				}
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		public void commandTaskDeleteByName(List<OptionMapping> options) { 
			/*
			 * 1. Check valid channel
			 * 2. Check if task actually exist (check according to channel and guild) 
			 * 3. Remove task from SQL 
			 * 4. Update discord hook
			 */
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			String name = options.get(1).getAsString();
			String guildId = e.getGuild().getId();
			String channelId = e.getTextChannel().getId();
			DiscTask task = null;
			
			//check for valid channel
			if(sqlModel.checkValidChannel(guildId, channelId)) {
				//check if task exist
				if(sqlModel.doesTaskExistByName(guildId, channelId, name)) {
					task = sqlModel.getTaskByName(guildId, channelId, name);
					task.retrieveNames(e.getGuild());
					sqlModel.deleteTaskByName(guildId, channelId, name);
					discordView.taskDeleted(hook);
					discordView.logTaskDeleted(e.getTextChannel(), e.getMember().getEffectiveName(), e.getUser().getEffectiveAvatarUrl(), task);
				} else {
					discordView.taskDoesntExist(hook);
				}
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		public void commandTaskDeleteById(List<OptionMapping> options) { 
			/*
			 * 1. Check valid channel
			 * 2. Check if task actually exist (check according to channel and guild) 
			 * 3. Remove task from SQL 
			 * 4. Update discord hook
			 */
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			String id = options.get(1).getAsString();
			String guildId = e.getGuild().getId();
			String channelId = e.getTextChannel().getId();
			DiscTask task;
			
			//check for valid channel
			if(sqlModel.checkValidChannel(guildId, channelId)) {
				try {
					//check if task exist
					if(sqlModel.doesTaskExistById(guildId, channelId, id)) {
						task = sqlModel.getTaskById(guildId, channelId, Integer.parseInt(id));
						task.retrieveNames(e.getGuild());
						sqlModel.deleteTaskById(guildId, channelId, id);
						discordView.taskDeleted(hook);
						discordView.logTaskDeleted(e.getTextChannel(), e.getMember().getEffectiveName(), e.getUser().getEffectiveAvatarUrl(), task);
					} else {
						discordView.taskDoesntExist(hook);
					}
				} catch (NumberFormatException e) { 
					discordView.notAValidNumber(hook, options.get(1).getAsString());
				}
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		void commandViewByName(List<OptionMapping> options) {
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			String name = options.get(1).getAsString();
			
			if(sqlModel.checkValidChannel(e.getGuild().getId(), e.getTextChannel().getId())) {
				DiscTask task = sqlModel.getTaskByName(e.getGuild().getId(), e.getTextChannel().getId(), name);
				
				//get names of author adn assigned from their IDs
				if(task != null) { 
					task.retrieveNames(e.getGuild());
					discordView.taskViewByName(hook, task, name);
				} else {
					discordView.taskDoesntExistByName(hook, name);
				}
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		void commandViewById(List<OptionMapping> options) {
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();

			
			if(sqlModel.checkValidChannel(e.getGuild().getId(), e.getTextChannel().getId())) {
				try { 
					int id = Integer.parseInt(options.get(1).getAsString());
				
					DiscTask task = sqlModel.getTaskById(e.getGuild().getId(), e.getTextChannel().getId(), id);
					
					//get names of author adn assigned from their IDs
					if(task != null) { 
						task.retrieveNames(e.getGuild());
						discordView.taskViewById(hook, task, id);
					} else {
						discordView.taskDoesntExistById(hook, id);
					}
				} catch (Exception e) { 
					discordView.notAValidNumber(hook, options.get(1).getAsString());
				}
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		void commandViewByStatus(List<OptionMapping> options) {
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			Guild guild = e.getGuild();
			
			if(sqlModel.checkValidChannel(e.getGuild().getId(), e.getTextChannel().getId())) {
				try { 
					int status = Integer.parseInt(options.get(1).getAsString());
					List<DiscTask> tasks = sqlModel.getTasksByStatus(guild.getId(), e.getTextChannel().getId(), status);
					//get names of author adn assigned from their IDs
					if(!tasks.isEmpty()) {
						for(DiscTask t : tasks) { 
							t.retrieveNames(e.getGuild());
							if(!t.getAssigned().isEmpty()) {
								String assigned = guild.getMemberById(t.getAssigned()).getEffectiveName();
								t.setAssigned(assigned);
							}
						}
						discordView.taskViewAll(hook, tasks);
					} else {
						discordView.taskEmpty(hook);
					}
				} catch (NumberFormatException e) { 
					discordView.notAValidNumber(hook, options.get(1).getAsString());
				}
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		void commandViewAll() {
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			Guild guild = e.getGuild();
			
			if(sqlModel.checkValidChannel(e.getGuild().getId(), e.getTextChannel().getId())) {
				List<DiscTask> tasks = sqlModel.getAllTasks(guild.getId(), e.getTextChannel().getId());
				//get names of author adn assigned from their IDs
				if(!tasks.isEmpty()) {
					for(DiscTask t : tasks) { 
						t.retrieveNames(e.getGuild());
						if(!t.getAssigned().isEmpty()) {
							String assigned = guild.getMemberById(t.getAssigned()).getEffectiveName();
							t.setAssigned(assigned);
						}
					}
					discordView.taskViewAll(hook, tasks);
				} else {
					discordView.taskEmpty(hook);
				}
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		public void commandReminderSet(List<OptionMapping> options) { 
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			User targetUser = e.getUser();
			long dur = 0;
			Duration jDur = Duration.ZERO;
			String desc = "";
			DiscTask task = null;
			
			if(sqlModel.checkValidChannel(e.getGuild().getId(), e.getTextChannel().getId())) {
				//get task by id 
				if(options.get(0).getAsString().equalsIgnoreCase("id")) {
					try {
						int id = Integer.parseInt(options.get(1).getAsString().trim());
						task = sqlModel.getTaskById(e.getGuild().getId(), e.getTextChannel().getId(), id);
						if(task != null) { 
							task.retrieveNames(e.getGuild());
						} else {
							discordView.taskDoesntExistById(hook, id);
							return;
						}
					} catch (NumberFormatException e) { 
						discordView.notAValidNumber(hook, options.get(1).getAsString().trim());
						return;
					}
				//get task by name
				} else if (options.get(0).getAsString().equalsIgnoreCase("name")) {
					task = sqlModel.getTaskByName(e.getGuild().getId(), e.getTextChannel().getId(), options.get(1).getAsString());
					if(task!=null) { 
						task.retrieveNames(e.getGuild());
					} else {
						discordView.taskDoesntExistByName(hook, options.get(1).getAsString());
						return;
					}
				}
				//set guild name
				task.setGuild(e.getGuild().getName());
				//calculate duration
				for (int i = 2; i < options.size(); i++) {
					if(options.get(i).getName().equalsIgnoreCase("days")) {
						dur += Integer.parseInt(options.get(i).getAsString()) * DAYS_TO_SEC;
						jDur = jDur.plusDays(Integer.parseInt(options.get(i).getAsString()));
					} else if(options.get(i).getName().equalsIgnoreCase("hours")) {
						dur += Integer.parseInt(options.get(i).getAsString()) * HOURS_TO_SEC;
						jDur = jDur.plusHours(Integer.parseInt(options.get(i).getAsString()));
					} else if(options.get(i).getName().equalsIgnoreCase("minutes")) {
						dur += Integer.parseInt(options.get(i).getAsString()) * MINUTES_TO_SEC;
						jDur = jDur.plusMinutes(Integer.parseInt(options.get(i).getAsString()));
					} else if(options.get(i).getName().equalsIgnoreCase("seconds")) {
						dur += Integer.parseInt(options.get(i).getAsString()) * SECONDS_TO_SEC;
						jDur = jDur.plusSeconds(Integer.parseInt(options.get(i).getAsString()));
					}
					//if duraiton too long
					if (dur > MAX_REMINDER_DURATION || dur < 0) { 
						discordView.reminderTooLong(hook);
						return;
					}
					//if there is an assigned
					if(options.get(i).getName().equalsIgnoreCase("target")) {
						targetUser = options.get(i).getAsUser();
					}
					//if there is a description
					if (options.get(i).getName().equalsIgnoreCase("description")) { 
						desc = options.get(i).getAsString();
					}
				}
				//if no duration
				if(dur == 0 ) { 
					discordView.reminderTooLong(hook);
					return;
				}
				
				//if all passes then submit to executor service
				@SuppressWarnings("rawtypes")
				Future f;
				String[] dateTime = DateTimeUtility.getDateTimeOffset(jDur);
				Reminder r = new Reminder(task, targetUser, e.getMember(), e.getGuild().getMember(targetUser), reminderIdx, dateTime);
				//set description if exists
				if(!desc.isEmpty()) {
					r.setDescription(desc);
				}
				//submit to executor and get future reference
				f = reminderExecutor.schedule(r, dur, TimeUnit.SECONDS);
				r.setFuture(f);
				reminderMap.put(reminderIdx, r);
				reminderIdx++;
				//same user then announce in ephemeral 
				if(targetUser.getId().equals(e.getUser().getId())) {
					discordView.reminderSetEphemeral(hook, r);
				} else {
					discordView.reminderSetPublic(hook, r, e.getTextChannel());
				}
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		public void commandReminderView(List<OptionMapping> options) {
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			Collection<Reminder> reminders = reminderMap.values();
			List<Reminder> res = new ArrayList<Reminder>();
			
			for(Reminder r: reminders) {
				if(r.user.getId().equals(e.getUser().getId())) {
					res.add(r);
				}
			}
			
			discordView.reminderView(hook, res);
		}
		
		public void commandReminderDelete(List<OptionMapping> options) {
			e.deferReply(true).queue();
			int id = Integer.parseInt(options.get(0).getAsString());
			InteractionHook hook = e.getHook();
			Reminder targetReminder = reminderMap.get(id);
			
			if(targetReminder != null) { 
				targetReminder.future.cancel(false);
				reminderMap.remove(id);
				discordView.reminderCancelled(hook, targetReminder);
			} else {
				discordView.reminderDosentExist(hook, id);
			}
		}
		
		public void commandRepeatSet(List<OptionMapping> options) { 
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			int targetHr = Integer.parseInt(options.get(2).getAsString());
			int targetMin = Integer.parseInt(options.get(3).getAsString());
			long period = 0;
			String description = "";
			DiscTask task = null;
			
			//check valid channel
			if(sqlModel.checkValidChannel(e.getGuild().getId(), e.getTextChannel().getId())) {
				//get task by id 
				if(options.get(0).getAsString().equalsIgnoreCase("id")) {
					try {
						int id = Integer.parseInt(options.get(1).getAsString().trim());
						task = sqlModel.getTaskById(e.getGuild().getId(), e.getTextChannel().getId(), id);
						if(task != null) { 
							task.retrieveNames(e.getGuild());
						} else {
							discordView.taskDoesntExistById(hook, id);
							return;
						}
					} catch (NumberFormatException e) { 
						discordView.notAValidNumber(hook, options.get(1).getAsString().trim());
						return;
					}
				//get task by name
				} else if (options.get(0).getAsString().equalsIgnoreCase("name")) {
					task = sqlModel.getTaskByName(e.getGuild().getId(), e.getTextChannel().getId(), options.get(1).getAsString());
					if(task!=null) { 
						task.retrieveNames(e.getGuild());
					} else {
						discordView.taskDoesntExistByName(hook, options.get(1).getAsString());
						return;
					}
				}
				//set guild name
				task.setGuild(e.getGuild().getName());
				//validate the target time
				if(targetHr >= 24 || targetHr < 0 || targetMin < 0 || targetMin >= 60) {
					discordView.repeatInvalidTime(hook);
					return;
				}
				//get delay
				long delay = DateTimeUtility.getOffsetSeconds(targetHr, targetMin);
				//get period
				for(int i = 4;i < options.size(); i++) { 
					if(options.get(i).getName().equalsIgnoreCase("period_hour")) {
						period += Integer.parseInt(options.get(i).getAsString()) * HOURS_TO_SEC;
					} else if(options.get(i).getName().equalsIgnoreCase("period_min")) {
						period += Integer.parseInt(options.get(i).getAsString()) * MINUTES_TO_SEC;
					} else if(options.get(i).getName().equalsIgnoreCase("period_day")) {
						period += Integer.parseInt(options.get(i).getAsString()) * DAYS_TO_SEC;
					} else if(options.get(i).getName().equalsIgnoreCase("description")) {
						description = options.get(i).getAsString();
					}
				}
				//check if period is passed max limit
				if(period > MAX_REMINDER_DURATION) { 
					discordView.reminderTooLong(hook);
					return;
				}
				Repeater r = new Repeater(task, e.getUser(), e.getMember(), Duration.of(period, ChronoUnit.SECONDS), repeaterIdx);
				repeaterMap.put(repeaterIdx, r);
				repeaterIdx++;
				r.setDesc(description);
				@SuppressWarnings("rawtypes")
				Future f = repeaterExecutor.scheduleAtFixedRate(r, delay, period, TimeUnit.SECONDS);
				r.setFuture(f);
				r.setNextRepeat(DateTimeUtility.getDateTimeOffset(Duration.of(delay, ChronoUnit.SECONDS)));
				discordView.repeatSet(hook,r);
			} else {
				discordView.notAValidChannel(hook);
			}
		}
		
		public void commandRepeatView(List<OptionMapping> options) { 
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			Collection<Repeater> repeaters = repeaterMap.values();
			List<Repeater> res = new ArrayList<Repeater>();
			
			for(Repeater r : repeaters) {
				if(r.user.getId().equals(e.getUser().getId())) {
					res.add(r);
				}
			}
			
			discordView.repeaterView(hook, res);
		}
		
		public void commandRepeatDelete(List<OptionMapping> options) { 
			e.deferReply(true).queue();
			int id;
			InteractionHook hook = e.getHook();
			Repeater targetRepeater;
			
			try { 
				id = Integer.parseInt(options.get(0).getAsString());
				targetRepeater = repeaterMap.get(id);
				if(targetRepeater != null) { 
					targetRepeater.future.cancel(false);
					repeaterMap.remove(id);
					discordView.repeaterCancelled(hook, targetRepeater);
				} else {
					discordView.repeaterDosentExist(hook, id);
				}
			} catch (NumberFormatException e) { 
				discordView.notAValidNumber(hook, options.get(0).getAsString());
			}
			
		}
		
		void commandStatus(List<OptionMapping> options) { 
			e.deferReply(true).queue();
			InteractionHook hook = e.getHook();
			
			String firstOpt = options.get(0).getAsString();
			DiscTask task = null;
			
			try {
				int status = Integer.parseInt(options.get(2).getAsString());
				if(firstOpt.equalsIgnoreCase("id")) { 
					int id = Integer.parseInt(options.get(1).getAsString());
					task = sqlModel.updateTaskStatus(id, e.getGuild().getId(), e.getTextChannel().getId(), status);
				} else if (firstOpt.equalsIgnoreCase("name")) { 
					String name = options.get(1).getAsString();
					task = sqlModel.updateTaskStatus(name, e.getGuild().getId(), e.getTextChannel().getId(), status);
				}
				discordView.statusUpdated(hook, task);
				discordView.logTaskStatusUpdate(e.getTextChannel(), e.getMember().getEffectiveName(), e.getUser().getEffectiveAvatarUrl(), task);
			} catch (NumberFormatException e) { 
				discordView.notAValidNumber(hook, "");
			}
		}
	}
}
