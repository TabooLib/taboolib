@file:Isolated

package taboolib.platform.compat

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.common.Isolated

val isEconomySupported: Boolean
    get() = VaultService.economy != null

val isPermissionSupported: Boolean
    get() = VaultService.permission != null

fun OfflinePlayer.getBalance(): Double {
    return VaultService.economy!!.getBalance(this)
}

fun OfflinePlayer.hasAccount(): Boolean {
    return VaultService.economy!!.hasAccount(this)
}

fun OfflinePlayer.createAccount(): Boolean {
    return VaultService.economy!!.createPlayerAccount(this)
}

fun OfflinePlayer.depositBalance(amount: Double): EconomyResponse {
    val response = VaultService.economy!!.depositPlayer(this, amount)
    return EconomyResponse(response.amount, response.balance, EconomyResponse.ResponseType.values()[response.type.ordinal], response.errorMessage)
}

fun OfflinePlayer.withdrawBalance(amount: Double): EconomyResponse {
    val response = VaultService.economy!!.withdrawPlayer(this, amount)
    return EconomyResponse(response.amount, response.balance, EconomyResponse.ResponseType.values()[response.type.ordinal], response.errorMessage)
}

fun Player.checkPermission(permission: String): Boolean {
    return VaultService.permission!!.playerHas(this, permission)
}

fun Player.checkGroup(group: String): Boolean {
    return VaultService.permission!!.playerInGroup(this, group)
}

fun Player.getGroups(): List<String> {
    return VaultService.permission!!.getPlayerGroups(this).toList()
}

fun Player.getPrimaryGroup(): String {
    return VaultService.permission!!.getPrimaryGroup(this)
}

@Isolated
object VaultService {

    var economy: Economy? = null
    var permission: Permission? = null

    init {
        if (Bukkit.getServer().pluginManager.getPlugin("Vault") != null) {
            economy = Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider
            permission = Bukkit.getServer().servicesManager.getRegistration(Permission::class.java)?.provider
        }
    }
}

@Isolated
data class EconomyResponse(val amount: Double, val balance: Double, val type: ResponseType, val errorMessage: String?) {

    enum class ResponseType(val id: Int) {

        SUCCESS(1), FAILURE(2), NOT_IMPLEMENTED(3);
    }

    fun transactionSuccess(): Boolean {
        return type == ResponseType.SUCCESS
    }
}