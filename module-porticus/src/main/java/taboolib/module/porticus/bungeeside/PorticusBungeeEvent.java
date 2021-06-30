package taboolib.module.porticus.bungeeside;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.module.porticus.Porticus;
import taboolib.module.porticus.common.Response;

import java.util.UUID;

/**
 * @author 坏黑
 * @since 2018-04-16
 */
public class PorticusBungeeEvent extends Event implements Cancellable, Response {
	
	private final Server sender;
	private final String[] args;
	private final UUID uid;
	private boolean cancel;

	public static void call(Server sender, UUID uid, String[] args) {
		BungeeCord.getInstance().getPluginManager().callEvent(new PorticusBungeeEvent(sender, uid, args));
	}
	
	PorticusBungeeEvent(Server sender, UUID uid, String[] args) {
		this.sender = sender;
		this.args = args;
		this.uid = uid;
	}

	@Override
	public void response(String... args) {
		Porticus.INSTANCE.getAPI().createMission(uid).command(args).run(sender);
	}

	@Override
	public boolean isCancelled() {
		return this.cancel;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@NotNull
	public String get(int index) {
		return this.args[index];
	}

	@NotNull
	public String getOrElse(int index, String orElse) {
		return index < this.args.length ? this.args[index] : orElse;
	}

	@Nullable
	public String getOrNull(int index) {
		return index < this.args.length ? this.args[index] : null;
	}

	public Server getSender() {
		return sender;
	}

	public String[] getArgs() {
		return this.args;
	}

	public UUID getUID() {
		return this.uid;
	}
}
