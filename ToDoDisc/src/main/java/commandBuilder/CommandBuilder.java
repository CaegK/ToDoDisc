package commandBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class CommandBuilder {
	// COMMAND TYPES
	final static int COMMAND_TYPE_CHAT = 1;
	final static int COMMAND_TYPE_USER = 2;
	final static int COMMAND_TYPE_MESSAGE = 3;
	// COMMAND OPTION TYPES
	final static int COMMAND_OPTION_SUB_COMMAND = 1;
	final static int COMMAND_OPTION_SUB_COMMAND_GROUP = 2;
	final static int COMMAND_OPTION_STRING = 3;
	final static int COMMAND_OPTION_INTEGER = 4;
	final static int COMMAND_OPTION_BOOLEAN = 5;
	final static int COMMAND_OPTION_USER = 6;
	final static int COMMAND_OPTION_CHANNEL = 7;
	final static int COMMAND_OPTION_ROLE = 8;
	final static int COMMAND_OPTION_MENTIONABLE = 9;
	final static int COMMAND_OPTION_NUMBER = 10;
	//COMMANDS
	final public static String[] COMMANDS = {"taskcategory", "task", "view", "reminder", "repeat", "status"};
	
	HttpClient client;
	HttpRequest req;
	String token;
	String appId;
	String guildId;
	Gson gson;
	
	public CommandBuilder(String token, String appId, String guildId) {
		this.token = token;
		this.appId = appId;
		this.guildId = guildId;
		client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(30))
				.build();
		gson = new Gson();
	}
	
	public int[] updateAllCommands() { 
		int[] res = new int[6];
		
		try {
		res[0] = updateTaskCatCommand();
		Thread.sleep(5000);
		res[1] = updateTaskCommand();
		Thread.sleep(5000);
		res[2] = updateViewCommand();
		Thread.sleep(5000);
		res[3] = updateReminderCommand();
		Thread.sleep(5000);
		res[4] = updateRepeatCommand();
		Thread.sleep(5000);
		res[5] = updateStatusCommand();
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
		return res;
	}
	
	public int updateTaskCatCommand() { 
		/*
		 *  TASKCATEGORY COMMANDS
		 */
		// /taskcategory
		Command taskCatCommand = new Command(COMMAND_TYPE_CHAT, "taskcategory", "Methods to create, edit and delete task categories", true);
		// /taskcategory [create/delete]
		CommandOption taskCatCreateSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "create", "Create a task category", true);
		CommandOption taskCatDelSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "delete", "Deletes a task category", true);
		// /taskcategory create [name] <description>
		CommandOption taskCatCreateName = new CommandOption(COMMAND_OPTION_STRING, "name", "Task category name", true);
		CommandOption taskCatCreateDesc = new CommandOption(COMMAND_OPTION_STRING, "description", "Task category description", false);
		
		taskCatCreateSub.setOptions(List.of(taskCatCreateName, taskCatCreateDesc));
		taskCatCommand.setOptions(List.of(taskCatCreateSub, taskCatDelSub));
		
		return updateCommands(taskCatCommand);
	}
	
//	public int updateTaskCommand() {
//		/*
//		 *  TASK COMMANDS
//		 */
//		// /task
//		Command taskCommand = new Command(COMMAND_TYPE_CHAT, "task", "Manage or view tasks. Can only be used in task channels.", true);
//		//subcommands /task [create/delete]
//		CommandOption taskCreateSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "create", "Create a task in this task category", true);
//		CommandOption taskDeleteSubGrp = new CommandOption(COMMAND_OPTION_SUB_COMMAND_GROUP, "delete", "Delete a task in this task category", false);
//		//create subcommand options  /task create [name] [description]
//		CommandOption taskCreateName = new CommandOption(COMMAND_OPTION_STRING, "name", "Name of task", true);
//		CommandOption taskCreateDesc = new CommandOption(COMMAND_OPTION_STRING, "description", "Description of task", false);
//		taskCreateSub.setOptions(List.of(taskCreateName, taskCreateDesc));
//		//delete subcommand group options /task delete [name/id] 
//		CommandOption taskDeleteNameSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "name", "Delete a task based on it's name", true);
//		CommandOption taskDeleteNameSubOpt = new CommandOption(COMMAND_OPTION_STRING, "name", "Task's name", true);
//		taskDeleteNameSub.setOptions(List.of(taskDeleteNameSubOpt));
//		
//		CommandOption taskDeleteIdSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "id", "Delete a task based on it's ID", true);
//		CommandOption taskDeleteIdSubOpt = new CommandOption(COMMAND_OPTION_INTEGER, "id", "Task's id", true);
//		taskDeleteIdSub.setOptions(List.of(taskDeleteIdSubOpt));
//		
//		taskDeleteSubGrp.setOptions(List.of(taskDeleteNameSub, taskDeleteIdSub));
//		
//		taskCommand.setOptions(List.of(taskCreateSub, taskDeleteSubGrp));
//		
//		return updateCommands(taskCommand);
//	}
	
	public int updateTaskCommand() { 
		/*
		 * TASK COMMANDS
		 */
		// /task
		Command taskCommand = new Command(COMMAND_TYPE_CHAT, "task", "Manage or view tasks. Can only be used in task channels.", true);
		// /task [create/delete]
		CommandOption taskCreate = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "create", "Create a task", true);
		CommandOption taskDelete = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "delete", "Delete a task", true);
		taskCommand.setOptions(List.of(taskCreate, taskDelete));
		// /task [create] [name] <description>
		CommandOption taskName = new CommandOption(COMMAND_OPTION_STRING, "name", "Task name", true);
		CommandOption taskDesc = new CommandOption(COMMAND_OPTION_STRING, "description", "Task description", false);
		taskCreate.setOptions(List.of(taskName, taskDesc));
		// /task [delete] [id/name] 
		CommandOption taskIdNameCho = new CommandOption(COMMAND_OPTION_STRING, "id_type", "Identify a task by id or name", true);
		CommandChoice taskDelName = new CommandChoice("name", "name");
		CommandChoice taskDelId = new CommandChoice("id", "id");
		taskIdNameCho.setChoices(List.of(taskDelName, taskDelId));
		// /task [delete] [id/name] [input]
		CommandOption taskDelInput = new CommandOption(COMMAND_OPTION_STRING, "identifier", "Id or name", true);
		
		taskDelete.setOptions(List.of(taskIdNameCho, taskDelInput));
		
		return updateCommands(taskCommand);
	}
	
	public int updateViewCommand() { 
		/*
		 * VIEW COMMANDS
		 */
		
		// /view 
		Command viewCommand = new Command(COMMAND_TYPE_CHAT, "view", "View specific task(s) by name, id or status", true);
		// /view [id/name/all/status]
		CommandOption viewOpt = new CommandOption(COMMAND_OPTION_STRING, "criteria", "Search criteria", true);
		CommandChoice viewId = new CommandChoice("id", "id");
		CommandChoice viewName = new CommandChoice("name", "name");
		CommandChoice viewAll = new CommandChoice("all", "all");
		CommandChoice viewStatus = new CommandChoice("status", "status");
		viewOpt.setChoices(List.of(viewId, viewName, viewAll, viewStatus));
		// /view [id/name/all/status] [input]
		CommandOption viewInput = new CommandOption(COMMAND_OPTION_STRING, "identifier", "Task ID or name", true);
		// /view [id/name/all/status] [input] [name/id/status]
		CommandOption viewSortOpt = new CommandOption(COMMAND_OPTION_STRING, "sort", "Sort selected task by name, ID or status", false);
		CommandChoice viewSortId = new CommandChoice("id", "id");
		CommandChoice viewSortName = new CommandChoice("name", "name");
		CommandChoice viewSortStatus = new CommandChoice("status", "status");
		viewSortOpt.setChoices(List.of(viewSortId, viewSortName, viewSortStatus));
		
		viewCommand.setOptions(List.of(viewOpt, viewInput, viewSortOpt));
		return updateCommands(viewCommand);
	}
	
	public int updateReminderCommand() {
		/*
		 * REMINDER COMMANDS
		 */

		//Reminders /reminder
		Command remCommand = new Command(COMMAND_TYPE_CHAT, "reminder", "Sets or removes a reminder", true);
		// /reminder set
		CommandOption remSetSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "set", "Sets a reminder", true);
		// /reminder set [id/name]
		CommandOption remIdNameOpt = new CommandOption(COMMAND_OPTION_STRING, "identifier", "Identify task by id or name", true);
		CommandChoice remIdCho = new CommandChoice("id", "id");
		CommandChoice remNameCho = new CommandChoice("name", "name");
		remIdNameOpt.setChoices(List.of(remIdCho, remNameCho));
		// /reminder set [id/name] [input]
		CommandOption remInputOpt = new CommandOption(COMMAND_OPTION_STRING, "task", "Task Id/Name", true);
		// /reminder set [id/name] [input] <day> <time> <seconds> <assigned> <description>
		CommandOption remDayOpt = new CommandOption(COMMAND_OPTION_INTEGER, "days", "Countdown days", false);
		CommandOption remHourOpt = new CommandOption(COMMAND_OPTION_INTEGER, "hours", "Countdown hours", false);
		CommandOption remMinOpt = new CommandOption(COMMAND_OPTION_INTEGER, "minutes", "Countdown minutes", false);
		CommandOption remSecOpt = new CommandOption(COMMAND_OPTION_INTEGER, "seconds", "Countdown seconds", false);
		CommandOption remDescOpt = new CommandOption(COMMAND_OPTION_STRING, "description", "Reminder description", false);
		CommandOption remAssOpt = new CommandOption(COMMAND_OPTION_USER, "target", "Who to remind", false);
		remSetSub.setOptions(List.of(remIdNameOpt, remInputOpt, remDayOpt, remHourOpt, remMinOpt, remSecOpt,remDescOpt, remAssOpt));
		// /reminder view
		CommandOption remViewSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "view", "View your reminders", true);
		// /reminder delete id
		CommandOption remDelSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "delete", "Delete your reminders", true);
		CommandOption remDelId = new CommandOption(COMMAND_OPTION_INTEGER, "id", "Reminder ID", true);
		remDelSub.setOptions(List.of(remDelId));
		
		remCommand.setOptions(List.of(remDelSub, remViewSub, remSetSub));
		
		return updateCommands(remCommand);
	}
	
	public int updateRepeatCommand() {
		/*
		 * REPEAT COMMANDs /repeat set [id/name] [input] [target hr] [target min] <day> <hour> <min> <description>
		 */
		
		// /repeat
		Command repeatCommand = new Command(COMMAND_TYPE_CHAT, "repeat", "A repeated reminder", true);
		// /repeat set
		CommandOption repeatSetSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "set", "Set a repeated reminder", true);
		// /repeat set [id/name] 
		CommandOption repeatIdNameOpt = new CommandOption(COMMAND_OPTION_STRING, "id_type", "Identify task by id or name", true);
		CommandChoice repeatIdCho = new CommandChoice("id", "id");
		CommandChoice repeatNameCho = new CommandChoice("name", "name");
		repeatIdNameOpt.setChoices(List.of(repeatIdCho, repeatNameCho));
		// /repeat set [id/name] [input] [targethour] [targetmin]
		CommandOption repeatInputOpt = new CommandOption(COMMAND_OPTION_STRING, "identifier", "id or name", true);
		CommandOption repeatTHrOpt = new CommandOption(COMMAND_OPTION_INTEGER, "target_hour", "Targetted hour", true);
		CommandOption repeatTMinOpt = new CommandOption(COMMAND_OPTION_INTEGER, "target_min", "Targetted minute", true);
		// /repeat set [id/name] [input] [targethour] [targetmin] <day> <hour> <min> <description>
		CommandOption repeatDayOpt = new CommandOption(COMMAND_OPTION_INTEGER, "period_day", "Period duration, hours", false);
		CommandOption repeatHourOpt = new CommandOption(COMMAND_OPTION_INTEGER, "period_min", "Period duration, minutes", false);
		CommandOption repeatMinOpt = new CommandOption(COMMAND_OPTION_INTEGER, "period_hour", "Period duration, seconds", false);
		CommandOption repeatDescOpt = new CommandOption(COMMAND_OPTION_STRING, "description", "Description", false);
		// /repeat delete [id]
		CommandOption repeatDelSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "delete", "Delete a repeated reminder", true);
		CommandOption repeatDelIdOpt = new CommandOption(COMMAND_OPTION_INTEGER, "id", "Repeater ID", true);
		repeatDelSub.setOptions(List.of(repeatDelIdOpt));
		// /repeat view
		CommandOption repeatViewSub = new CommandOption(COMMAND_OPTION_SUB_COMMAND, "view", "View your repeated reminders", true);
		
		
		repeatSetSub.setOptions(List.of(repeatIdNameOpt, repeatInputOpt, repeatTHrOpt, repeatTMinOpt, repeatDayOpt, repeatHourOpt, repeatMinOpt, repeatDescOpt));
		repeatCommand.setOptions(List.of(repeatSetSub, repeatDelSub, repeatViewSub));
		return updateCommands(repeatCommand);
	}
	
	public int updateStatusCommand() { 
		/*
		 * STATUS COMMAND /status [id/name] [input] [status]
		 */
		// /status
		Command statusCommand = new Command(COMMAND_TYPE_CHAT, "status", "change the status of a task", true);
		// /status [id/name]
		CommandOption statusIdNameOpt = new CommandOption(COMMAND_OPTION_STRING, "id_type", "Identify a task by ID or name", true);
		CommandChoice statusIdCho = new CommandChoice("id", "id");
		CommandChoice statusNameCho = new CommandChoice("name", "name");
		statusIdNameOpt.setChoices(List.of(statusIdCho, statusNameCho));
		// /status [id/name] [input]
		CommandOption statusInputOpt = new CommandOption(COMMAND_OPTION_STRING, "identifier", "id or name", true);
		// /status [id/name] [input] [status]
		CommandOption statusStatusOpt = new CommandOption(COMMAND_OPTION_INTEGER, "status", "status code to update to", true);
		
		statusCommand.setOptions(List.of(statusIdNameOpt, statusInputOpt, statusStatusOpt));
		
		return updateCommands(statusCommand);
	}
	
	private String getCommands() {
		req = HttpRequest.newBuilder()
				.uri(URI.create("https://discord.com/api/v8/applications/"+appId+ "/guilds/"+ guildId + "/commands"))
				.header("Content-Type", "application/json")
				.header("Authorization" , "Bot " + token)
				.GET()
				.build();
		try {
			HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());
			return resp.body();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return "Error on retrieving response";
		} 
	}
	
	private void deleteCommand(String id) {
		req = HttpRequest.newBuilder()
				.uri(URI.create("https://discord.com/api/v8/applications/"+appId+"/guilds/"+ guildId + "/commands/"+id))
				.header("Content-Type", "application/json")
				.header("Authorization" , "Bot " + token)
				.DELETE()
				.build();
		try {
			HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());
			int respCode = resp.statusCode();
			System.out.println("Delete response code: " + respCode);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	private int updateCommands(Command c) {
		try {
			req = HttpRequest.newBuilder()
					.uri(URI.create("https://discord.com/api/v8/applications/"+appId+"/guilds/"+ guildId + "/commands"))
					.header("Content-Type", "application/json")
					.header("Authorization" , "Bot " + token)
//					.POST(BodyPublishers.ofFile(Paths.get("src/main/resources/test2.json")))
					.POST(BodyPublishers.ofString(gson.toJson(c)))
					.build();
			HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());
			System.out.println("Upsert commands response code: " + resp.statusCode());
			System.out.println("Upsert commands response body: " + resp.body());
			return resp.statusCode();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
	}
	
	private static class Command {
		int type;
		String guild_id;
		String name;
		String description;
		List<CommandOption> options;
		boolean default_permissions;
		
		public Command(int type, String name, String description, boolean default_permissions) {
			this.type = type;
			this.name = name;
			this.description = description;
			this.default_permissions = default_permissions;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getGuildId() {
			return guild_id;
		}

		public void setGuildId(String guild_id) {
			this.guild_id = guild_id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public List<CommandOption> getOptions() {
			return options;
		}

		public void setOptions(List<CommandOption> options) {
			this.options = options;
		}

		public boolean isDefaultPermissions() {
			return default_permissions;
		}

		public void setDefaultPermissions(boolean default_permissions) {
			this.default_permissions = default_permissions;
		}
		
		
	}
	
	private static class CommandOption{
		int type;
		String name;
		String description;
		boolean required;
		List<CommandChoice> choices = new ArrayList<CommandChoice>();
		List<CommandOption> options = new ArrayList<CommandOption>();
		
		public CommandOption(int type, String name, String description, boolean required) {
			this.type = type;
			this.name = name;
			this.description = description;
			if (type != COMMAND_OPTION_SUB_COMMAND) {
				this.required = required;
			}
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public List<CommandChoice> getChoices() {
			return choices;
		}

		public void setChoices(List<CommandChoice> choices) {
			this.choices = choices;
		}

		public List<CommandOption> getOptions() {
			return options;
		}

		public void setOptions(List<CommandOption> options) {
			this.options = options;
		}
		
		
	}
	
	private static class CommandChoice<T>{
		String name;
		T value;
		
		public CommandChoice(String name, T value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}
	}
}
