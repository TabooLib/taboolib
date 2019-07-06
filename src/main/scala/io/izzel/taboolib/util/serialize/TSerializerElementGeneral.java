package io.izzel.taboolib.util.serialize;

import io.izzel.taboolib.util.lite.Numbers;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * @Author 坏黑
 * @Since 2019-03-10 18:49
 */
public enum TSerializerElementGeneral {

    STRING(new TSerializerElement() {

        @Override
        public String read(String value) {
            return String.valueOf(value);
        }

        @Override
        public String write(Object value) {
            return Objects.toString(value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return String.class.equals(objectClass);
        }
    }),

    INT(new TSerializerElement() {

        @Override
        public Integer read(String value) {
            try {
                return Integer.parseInt(value);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return 0;
        }

        @Override
        public String write(Object value) {
            return Objects.toString(value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return Integer.class.equals(objectClass) || Integer.TYPE.equals(objectClass);
        }
    }),

    LONG(new TSerializerElement() {

        @Override
        public Long read(String value) {
            try {
                return Long.parseLong(value);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return 0L;
        }

        @Override
        public String write(Object value) {
            return Objects.toString(value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return Long.class.equals(objectClass) || Long.TYPE.equals(objectClass);
        }
    }),

    SHORT(new TSerializerElement() {

        @Override
        public Short read(String value) {
            try {
                return Short.parseShort(value);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return (short) 0;
        }

        @Override
        public String write(Object value) {
            return Objects.toString(value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return Short.class.equals(objectClass) || Short.TYPE.equals(objectClass);
        }
    }),

    DOUBLE(new TSerializerElement() {

        @Override
        public Double read(String value) {
            try {
                return Double.parseDouble(value);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return 0D;
        }

        @Override
        public String write(Object value) {
            return Objects.toString(value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return Double.class.equals(objectClass) || Boolean.TYPE.equals(objectClass);
        }
    }),

    FLOAT(new TSerializerElement() {

        @Override
        public Float read(String value) {
            try {
                return Float.parseFloat(value);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return 0f;
        }

        @Override
        public String write(Object value) {
            return Objects.toString(value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return Float.class.equals(objectClass) || Float.TYPE.equals(objectClass);
        }
    }),

    BOOLEAN(new TSerializerElement() {

        @Override
        public Boolean read(String value) {
            try {
                return Numbers.getBoolean(value);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return false;
        }

        @Override
        public String write(Object value) {
            return Objects.toString(value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return Boolean.class.equals(objectClass) || Boolean.TYPE.equals(objectClass);
        }
    }),

    ITEM_STACK(new TSerializerElement() {

        @Override
        public ItemStack read(String value) {
            return TSerializer.deserializeCS(value, ItemStack.class);
        }

        @Override
        public String write(Object value) {
            return TSerializer.serializeCS((ItemStack) value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return ItemStack.class.isAssignableFrom(objectClass);
        }
    }),

    LOCATION(new TSerializerElement() {

        @Override
        public Location read(String value) {
            return TSerializer.deserializeCS(value, Location.class);
        }

        @Override
        public String write(Object value) {
            return TSerializer.serializeCS((Location) value);
        }

        @Override
        public boolean matches(Class objectClass) {
            return Location.class.isAssignableFrom(objectClass);
        }
    }),

    CUSTOM(new TSerializerElement() {

        @Override
        public Object read(String value) {
            return null;
        }

        @Override
        public String write(Object value) {
            return null;
        }

        @Override
        public boolean matches(Class objectClass) {
            return TSerializable.class.isAssignableFrom(objectClass);
        }
    });

    private TSerializerElement serializer;

    TSerializerElementGeneral(TSerializerElement serializer) {
        this.serializer = serializer;
    }

    public TSerializerElement getSerializer() {
        return serializer;
    }
}