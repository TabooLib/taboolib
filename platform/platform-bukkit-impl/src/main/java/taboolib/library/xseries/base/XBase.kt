/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package taboolib.library.xseries.base

import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract
import java.util.*
import java.util.stream.Collectors

/**
 * Do not use this class directly.
 *
 *
 * All XModules should implement the following static methods:
 * <pre>`public static final XRegistry<XAttribute, Attribute> REGISTRY;
 *
 * public static XForm of(@NotNull BukkitForm bukkit) {
 * return REGISTRY.getByBukkitForm(bukkit);
 * }
 *
 * public static Optional<XForm> of(@NotNull String bukkit) {
 * return REGISTRY.getByName(bukkit);
 * }
 *
 *
 *
 * public static XForm[] values() {
 * return REGISTRY.values();
 * }
 *
 *
 *
 * public static Collection<XAttribute> getValues() {
 * return REGISTRY.getValues();
 * }
`</pre> *
 * All these methods are available from their [XRegistry], however these are for
 * cross-compatibility (which will be removed later) and ease of use.
 *
 * @param <XForm>      the class type associated with the Bukkit type defined by XSeries.
 * @param <BukkitForm> the Bukkit class type associated with the XForm.
 * @see XModule
</BukkitForm></XForm> */
interface XBase<XForm : XBase<XForm, BukkitForm>?, BukkitForm> {
    /**
     * Should be used for saving data.
     */
    @Contract(pure = true)
    fun name(): String

    @get:Contract(pure = true)
    @get:ApiStatus.Internal
    val names: Array<String>

    /**
     * In most cases you should be using [.name] instead.
     *
     * @return a friendly readable string name.
     */
    @Contract(pure = true)
    fun friendlyName(): String {
        return Arrays.stream(name().split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .map { t: String -> t[0].toString() + t.substring(1).lowercase() }
            .collect(Collectors.joining(" "))
    }

    @Contract(pure = true)
    fun get(): BukkitForm?

    @get:Contract(pure = true)
    val isSupported: Boolean
        /**
         * Checks if this sound is supported in the current Minecraft version.
         *
         *
         * An invocation of this method yields exactly the same result as the expression:
         *
         *
         * <blockquote>
         * [.get] != null
        </blockquote> *
         *
         * @return true if the current version has this sound, otherwise false.
         * @since 1.0.0
         */
        get() = get() != null

    /**
     * Checks if this form is supported in the current version and
     * returns itself if yes.
     *
     *
     * In the other case, the alternate form will get returned,
     * no matter if it is supported or not.
     *
     * @param other the other form to get if this one is not supported.
     * @return this form or the `other` if not supported.
     */
    @Contract(pure = true)
    fun or(other: XForm): XForm {
        return if (this.isSupported) this as XForm else other
    }

    @get:ApiStatus.Internal
    val metadata: XModuleMetadata
        /**
         * Gets additional information which is only needed during initialization and will only
         * cause unnecessary memory consumptions when used during runtime.
         *
         * @see XRegistry.getOrRegisterMetadata
         */
        get() {
            val registry =
                XRegistry.registryOf(javaClass as Class<out XForm>)
            return registry!!.getOrRegisterMetadata(
                this as XForm,
                registry.getBackingField(this as XForm),
                false
            )
        }
}
