package me.skymc.taboolib.jsonformatter.click;

import me.skymc.taboolib.json.JSONObject;

public class RunCommandEvent extends ClickEvent{
	
	private JSONObject object = new JSONObject();
	
	public RunCommandEvent(String command){
		try{
			object.put("action", "run_command");
			object.put("value", command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject getEvent(){
		return object;
	}
	
}
