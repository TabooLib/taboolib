package me.skymc.taboolib.jsonformatter.hover;

import me.skymc.taboolib.json.JSONObject;

public class ShowTextEvent extends HoverEvent{
	
	private JSONObject object = new JSONObject();
	
	public ShowTextEvent(String text){
		try{
			object.put("action", "show_text");
			object.put("value", text);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject getEvent(){
		return object;
	}
	
}
