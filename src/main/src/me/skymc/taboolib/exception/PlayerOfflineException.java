package me.skymc.taboolib.exception;

public class PlayerOfflineException extends Error {

	private static final long serialVersionUID = 4129402767538548807L;
	
	public PlayerOfflineException(String message) {
		super(message);
	}
}
