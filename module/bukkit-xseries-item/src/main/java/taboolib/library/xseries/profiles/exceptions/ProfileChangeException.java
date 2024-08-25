package taboolib.library.xseries.profiles.exceptions;

import taboolib.library.xseries.profiles.builder.ProfileInstruction;

/**
 * Aggregate error container for {@link ProfileInstruction#apply()}.
 */
public final class ProfileChangeException extends ProfileException {
    public ProfileChangeException(String message) {
        super(message);
    }

    public ProfileChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}