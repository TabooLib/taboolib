package me.skymc.taboolib.jsonformatter.click;

import me.skymc.taboolib.json.JSONObject;

public class SuggestCommandEvent extends ClickEvent{
	
	private JSONObject object = new JSONObject();
	
	public SuggestCommandEvent(String suggest){
		try{
			object.put("action", "suggest_command");
			object.put("value", suggest);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject getEvent(){
		return object;
	}
	
}
