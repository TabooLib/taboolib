package taboolib.common.platform.event

import taboolib.common.Isolated

/**
 * Sponge Only
 *
 * TabooLib
 * taboolib.common.platform.event.EventOrder
 *
 * @author sky
 * @since 2021/6/21 6:24 下午
 */
@Isolated
enum class EventOrder {

    /**
     * The order point of PRE handles setting up things that need to be done
     * before other things are handled PRE is read only and cannot cancel the
     * events.
     */
    PRE,

    /**
     * The order point of AFTER_PRE handles things that need to be done after
     * PRE AFTER_PRE is read only and cannot cancel the events.
     */
    AFTER_PRE,

    /**
     * The order point of FIRST handles cancellation by protection plugins for
     * informational responses FIRST is read only but can cancel events.
     */
    FIRST,

    /**
     * The order point of EARLY handles standard actions that need to be done
     * before other plugins EARLY is not read only and can cancel events.
     */
    EARLY,

    /**
     * The order point of DEFAULT handles just standard event handlings, you
     * should use this unless you know you need otherwise DEFAULT is not read
     * only and can cancel events.
     */
    DEFAULT,

    /**
     * The order point of LATE handles standard actions that need to be done
     * after other plugins LATE is not read only and can cancel the event.
     */
    LATE,

    /**
     * The order point of LAST handles last minute cancellations by protection
     * plugins LAST is read only but can cancel events.
     */
    LAST,

    /**
     * The order point of BEFORE_POST handles preparation for things needing
     * to be done in post BEFORE_POST is read only and cannot cancel events.
     */
    BEFORE_POST,

    /**
     * The order point of POST handles last minute things and monitoring
     * of events for rollback or logging POST is read only and
     * cannot cancel events.</p>
     */
    POST

}