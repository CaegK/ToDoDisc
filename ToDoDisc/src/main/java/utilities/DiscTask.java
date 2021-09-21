package utilities;

import net.dv8tion.jda.api.entities.Guild;

public class DiscTask {
	String id;
	String category;
	String name;
	String date;
	String time;
	String author;
	String authorUrl;
	String assigned;
	String assignedUrl;
	String desc;
	String guild;
	int status;
	
	public String format() {
		
		String details = 
				"ID: `" + this.getId() + "`\n" + 
				"Status: `" + this.getStatus() + "`\n";
		
		//check for blank fields
		if(!this.getAssigned().isEmpty()) {
			details = details + "assigned to: `" + this.getAssigned() + "`\n";
		}
		if(!this.getDesc().isEmpty()) {
			details = details + "Description: `" + this.getDesc() + "`\n";
		}	
		
		details = details + "created by `" + this.getAuthor() + "` on `" + this.getDate() + "` at `" + this.getTime() + "`\n\n";
		return details;
	}
	
	public String formatGuild() {
		
		String details = 
				"ID: `" + this.getId() + "`\n" + 
				"Status: `" + this.getStatus() + "`\n";
		
		//check for blank fields
		if(!this.getAssigned().isEmpty()) {
			details = details + "assigned to: `" + this.getAssigned() + "`\n";
		}
		details = details + "Category: `" + this.getCategory() + "` \n";
		if(this.getGuild()!=null && !this.getGuild().isEmpty()) {
			details = details + "Guild: `" + this.getGuild() + "` \n";
		}
		if(!this.getDesc().isEmpty()) {
			details = details + "Description: `" + this.getDesc() + "`\n";
		}	
		
		details = details + "created by `" + this.getAuthor() + "` on `" + this.getDate() + "` at `" + this.getTime() + "`\n\n";
		
		return details;
	}
	
	//converts the authorId and assignedId to the effective name of the task 
	public void retrieveNames(Guild guild) { 
		setAuthor(guild.getMemberById(getAuthor().trim()).getEffectiveName());
		if(!getAssigned().isEmpty()) {
			setAuthor(guild.getMemberById(getAssigned()).getEffectiveName());
		}
	}
	
	public DiscTask(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}

	public String getCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}

	public String getTime() {
		return time;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getAuthorUrl() {
		return authorUrl;
	}

	public String getAssigned() {
		return assigned;
	}
	
	public String getAssignedUrl() {
		return assignedUrl;
	}

	public String getDesc() {
		return desc;
	}

	public int getStatus() {
		return status;
	}
	
	public String getGuild() { 
		return guild;
	}

	public DiscTask setId(String id) { 
		this.id = id;
		return this;
	}
	
	public DiscTask setCategory(String category) { 
		this.category = category;
		return this;
	}
	
	public DiscTask setDate(String date) { 
		this.date = date;
		return this;
	}
	
	public DiscTask setTime(String time) { 
		this.time = time;
		return this;
	}
	
	public DiscTask setAuthor(String author) { 
		this.author = author;
		return this;
	}
	
	public DiscTask setAuthorUrl(String authorUrl) { 
		this.authorUrl = authorUrl;
		return this;
	}
	
	public DiscTask setAssigned(String assigned) { 
		this.assigned = assigned;
		return this;
	}
	
	public DiscTask setAssignedUrl(String assignedUrl) { 
		this.assignedUrl = assignedUrl;
		return this;
	}
	
	public DiscTask setStatus(int status) { 
		this.status = status;
		return this;
	}
	
	public DiscTask setDesc(String desc) { 
		this.desc = desc;
		return this;
	}
	
	public DiscTask setGuild(String guild) { 
		this.guild = guild;
		return this;
	}
}
