package io.izzel.taboolib.util.item;

import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
import org.bukkit.map.MapView.Scale;
import org.bukkit.plugin.AuthorNagException;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author LagBug
 */
public class MapBuilder {

    public static final String VERSION = "1.5";
    private MapView map;
    private BufferedImage image;
    private List<Text> texts;
    private MapCursorCollection cursors;
    
    private boolean rendered;
    private boolean renderOnce;
    private boolean isNewVersion;

    public MapBuilder() {
        cursors = new MapCursorCollection();
        texts = new ArrayList<>();
        rendered = false;
        renderOnce = true;
        isNewVersion = Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.13");
    }

    /**
     * Get the image that's being used
     *
     * @return the image used
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Set an image to be used
     *
     * @param image the buffered image to use
     * @return the instance of this class
     */
    public MapBuilder setImage(@Nonnull BufferedImage image) {
        this.image = image;
        return this;
    }

    /**
     * Set and image to be used
     *
     * @param x, y the coordinates to add the text
     * @param font the font to be used
     * @param text the string that will be displayed
     * @return the instance of this class
     */
    public MapBuilder addText(@Nonnull int x, @Nonnull int y, @Nonnull MapFont font, @Nonnull String text) {
        this.texts.add(new Text(x, y, font, text));
        return this;
    }

    /**
     * Gets the list of all the texts used
     *
     * @return a List of all the texts
     */
    public List<Text> getTexts() {
        return texts;
    }

    /**
     * Adds a cursor to the map
     *
     * @param x, y the coordinates to add the cursor
     * @param direction the direction to display the cursor
     * @param type the type of the cursor
     * @return the instance of this class
     */
    @SuppressWarnings("deprecation")
    public MapBuilder addCursor(@Nonnull int x, @Nonnull int y, @Nonnull CursorDirection direction, @Nonnull CursorType type) {
        cursors.addCursor(x, y, (byte) direction.getId(), (byte) type.getId());
        return this;
    }

    /**
     * Gets all the currently used cursors
     *
     * @return a MapCursorCollection with all current cursors
     */
    public MapCursorCollection getCursors() {
        return cursors;
    }

    /**
     * Sets whether the image should only be rendered once.
     * Good for static images and reduces lag.
     *
     * @param renderOnce the value to determine if it's going to be rendered once
     * @return the instance of this class
     */
    public MapBuilder setRenderOnce(@Nonnull boolean renderOnce) {
        this.renderOnce = renderOnce;
        return this;
    }

    /**
     * Builds an ItemStack of the map.
     *
     * @return the ItemStack of the map containing what's been set from the above methods
     */
    @SuppressWarnings("deprecation")
    public ItemStack build() {
        ItemStack item = null;
        
        try {
        	item = new ItemStack(isNewVersion ? Material.MAP : Material.valueOf("MAP")); 
        } catch (AuthorNagException ex) {
            System.out.println("Could not get material for the current spigot version. This won't be shown again until server restats");
        }
                
        map = Bukkit.createMap(Bukkit.getWorlds().get(0));
        
        map.setScale(Scale.NORMAL);
        map.getRenderers().forEach(map::removeRenderer);
        map.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                if (rendered && renderOnce) {
                    return;
                }
                
                if (player != null && player.isOnline()) {
                    if (image != null) {
                        mapCanvas.drawImage(0, 0, image);
                    }
                    
                    if (!texts.isEmpty()) {
                    	texts.forEach(text -> mapCanvas.drawText(text.getX(), text.getY(), text.getFont(), text.getMessage()));	
                    }
                    
                    if (cursors.size() > 0) {
                    	mapCanvas.setCursors(cursors);	
                    }
                    
                    rendered = true;
                }
            }
        });

        if (isNewVersion) {
            MapMeta mapMeta = (MapMeta) item.getItemMeta();
            try {
                Reflection.invokeMethod(mapMeta, "setMapView", map);
            } catch (Throwable ignored) {
            }
            item.setItemMeta(mapMeta);
        } else {
            item.setDurability(getMapId(map));
        }
        return item;
    }

    /**
     * Gets a map id cross-version using reflection
     *
     * @param mapView the map to get the id
     * @return the instance of this class
     */
    private short getMapId(@Nonnull MapView mapView) {
        try {
            return (short) mapView.getId();
        } catch (NoSuchMethodError ex) {
            try {
                return (short) Class.forName("org.bukkit.map.MapView").getMethod("getId", (Class<?>[]) new Class[0])
                        .invoke(mapView, new Object[0]);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    /**
     * An enum containing user friendly cursor directions. Instead of using the integers, you can instead use this enum
     */
    public enum CursorDirection {
        SOUTH(0), SOUTH_WEST_SOUTH(1), SOUTH_WEST(2), SOUTH_WEST_WEST(3), WEST(4), NORTH_WEST_WEST(5), NORTH_WEST(6),
        NORTH_WEST_NORTH(7), NORTH(8), NORTH_EAST_NORTH(9), NORTH_EAST(10), NORTH_EAST_EAST(11), EAST(12),
        SOUTH_EAST_EAST(13), SOUNT_EAST(14), SOUTH_EAST_SOUTH(15);

        private final int id;

        CursorDirection(@Nonnull int id) {
            this.id = id;
        }

        /**
         * Returns the actual integer to use
         *
         * @return the integer of the specified enum type 
         */
        public int getId() {
            return this.id;
        }
    }

    /**
     * An enum containing user friendly cursor types. Instead of using the integers, you can instead use this enum
     */
    public enum CursorType {
        WHITE_POINTER(0), GREEN_POINTER(1), RED_POINTER(2), BLUE_POINTER(3), WHITE_CLOVER(4), RED_BOLD_POINTER(5),
        WHITE_DOT(6), LIGHT_BLUE_SQUARE(7);

        private final int id;

        CursorType(@Nonnull int id) {
            this.id = id;
        }

        /**
         * Returns the actual integer to use
         *
         * @return the integer of the specified enum type 
         */
        public int getId() {
            return this.id;
        }
    }

    /**
     * A storage class to save text information to later be used in order to write in maps
     */
    public static class Text {

        private int x;
        private int y;
        private MapFont font;
        private String message;

        public Text(@Nonnull int x, @Nonnull int y, @Nonnull MapFont font, @Nonnull String message) {
            setX(x);
            setY(y);
            setFont(font);
            setMessage(message);
        }

        /**
         * Gets the x position for the text to be displayed
         *
         * @return the x position
         */
        public int getX() {
            return x;
        }

        /**
         * Sets the x position of the text to display it
         *
         * @param x the x postion
         */
        public void setX(@Nonnull int x) {
            this.x = x;
        }

        /**
         * Gets the y position for the text to be displayed
         *
         * @return the y position
         */
        public int getY() {
            return y;
        }

        /**
         * Sets the y position of the text to display it
         *
         * @param y the y position
         */
        public void setY(@Nonnull int y) {
            this.y = y;
        }

        /**
         * Gets the font to be used
         *
         * @return the MapFont that is used
         */
        public MapFont getFont() {
            return font;
        }

        /**
         * Sets what font should be used
         *
         * @param font the actual font
         */
        public void setFont(@Nonnull MapFont font) {
            this.font = font;
        }

        /**
         * Gets what test will be displayed
         *
         * @return the text
         */
        public String getMessage() {
            return message;
        }

        /**
         * Sets what text will be displayed
         *
         * @param message the actual text
         */
        public void setMessage(@Nonnull String message) {
            this.message = message;
        }
    }
}