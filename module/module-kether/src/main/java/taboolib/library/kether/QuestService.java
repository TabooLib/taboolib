package taboolib.library.kether;

import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

public interface QuestService<CTX extends QuestContext> {

    QuestRegistry getRegistry();

    Optional<Quest> getQuest(String id);

    Map<String, Object> getQuestSettings(String id);

    Map<String, Quest> getQuests();

    void startQuest(CTX context);

    void terminateQuest(CTX context);

    Multimap<String, CTX> getRunningQuests();

    List<CTX> getRunningQuests(String playerIdentifier);

    Executor getExecutor();

    ScheduledExecutorService getAsyncExecutor();

    String getLocalizedText(String node, Object... params);

    @SuppressWarnings("unchecked")
    static <C extends QuestContext> QuestService<C> instance() {
        return (QuestService<C>) ServiceHolder.getQuestServiceInstance();
    }
}