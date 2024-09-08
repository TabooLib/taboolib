package taboolib.library.kether;

public class ServiceHolder {

    private static QuestService<?> questServiceInstance;

    public static void setQuestServiceInstance(QuestService<?> service) {
        ServiceHolder.questServiceInstance = service;
    }

    public static QuestService<?> getQuestServiceInstance() {
        return questServiceInstance;
    }
}