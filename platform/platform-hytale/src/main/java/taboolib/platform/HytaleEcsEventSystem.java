package taboolib.platform;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.platform.event.HytaleEcsContext;

import java.util.function.BiConsumer;

/**
 * TabooLib ECS 事件系统包装器
 *
 * @param <T> 事件类型
 * @author sky
 */
public class HytaleEcsEventSystem<T extends EcsEvent> extends EntityEventSystem<EntityStore, T> {

    private final BiConsumer<HytaleEcsEventContext, T> handler;

    public HytaleEcsEventSystem(@NotNull Class<T> eventType, @NotNull BiConsumer<HytaleEcsEventContext, T> handler) {
        super(eventType);
        this.handler = handler;
    }

    @Override
    public void handle(int index, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull T event) {
        handler.accept(new HytaleEcsEventContext(index, archetypeChunk, store, commandBuffer), event);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    /**
     * ECS 事件上下文
     */
    public static class HytaleEcsEventContext implements HytaleEcsContext<ArchetypeChunk<EntityStore>, Store<EntityStore>, CommandBuffer<EntityStore>> {

        private final int index;
        private final ArchetypeChunk<EntityStore> archetypeChunk;
        private final Store<EntityStore> store;
        private final CommandBuffer<EntityStore> commandBuffer;

        public HytaleEcsEventContext(int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
            this.index = index;
            this.archetypeChunk = archetypeChunk;
            this.store = store;
            this.commandBuffer = commandBuffer;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @NotNull
        @Override
        public ArchetypeChunk<EntityStore> getArchetypeChunk() {
            return archetypeChunk;
        }

        @NotNull
        @Override
        public Store<EntityStore> getStore() {
            return store;
        }

        @NotNull
        @Override
        public CommandBuffer<EntityStore> getCommandBuffer() {
            return commandBuffer;
        }
    }
}
