package me.skymc.taboolib.common.pathfinder;

/**
 * @Author sky
 * @Since 2018-09-19 19:42
 */
public abstract class SimpleAI {

    public abstract boolean shouldExecute();

    public boolean continueExecute() {
        return shouldExecute();
    }

    public void startTask() {
    }

    public void resetTask() {
    }

    public void updateTask() {
    }
}
