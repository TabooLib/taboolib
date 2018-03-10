package me.skymc.taboolib.jsonformatter.hover;

import me.skymc.taboolib.json.JSONObject;

public class ShowAchievementEvent extends HoverEvent{
	
	private JSONObject object = new JSONObject();
	
	public ShowAchievementEvent(String achievement){
		try{
			object.put("action", "show_achievement");
			object.put("value", achievement);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject getEvent(){
		return object;
	}
	
}
