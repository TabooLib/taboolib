package taboolib.model.chatcomponent;

/**
 * This component displays the score based on a player score on the scoreboard.
 * <br>
 * The <b>name</b> is the name of the player stored on the scoreboard, which may
 * be a "fake" player. It can also be a target selector that <b>must</b> resolve
 * to 1 target, and may target non-player entities.
 * <br>
 * With a book, /tellraw, or /title, using the wildcard '*' in the place of a
 * name or target selector will cause all players to see their own score in the
 * specified objective.
 * <br>
 * <b>Signs cannot use the '*' wildcard</b>
 * <br>
 * These values are filled in by the server-side implementation.
 * <br>
 * As of 1.12.2, a bug ( MC-56373 ) prevents full usage within hover events.
 */
public final class ScoreComponent extends BaseComponent {

    /**
     * The name of the entity whose score should be displayed.
     */
    private String name;

    /**
     * The internal name of the objective the score is attached to.
     */
    private String objective;

    /**
     * The optional value to use instead of the one present in the Scoreboard.
     */
    private String value = "";

    /**
     * Creates a new score component with the specified name and objective.<br>
     * If not specifically set, value will default to an empty string;
     * signifying that the scoreboard value should take precedence. If not null,
     * nor empty, {@code value} will override any value found in the
     * scoreboard.<br>
     * The value defaults to an empty string.
     *
     * @param name      the name of the entity, or an entity selector, whose score
     *                  should be displayed
     * @param objective the internal name of the objective the entity's score is
     *                  attached to
     */
    public ScoreComponent(String name, String objective) {
        setName(name);
        setObjective(objective);
    }

    /**
     * Creates a score component from the original to clone it.
     *
     * @param original the original for the new score component
     */
    public ScoreComponent(ScoreComponent original) {
        super(original);
        setName(original.getName());
        setObjective(original.getObjective());
        setValue(original.getValue());
    }

    public ScoreComponent(String name, String objective, String value) {
        this.name = name;
        this.objective = objective;
        this.value = value;
    }

    @Override
    public ScoreComponent duplicate() {
        return new ScoreComponent(this);
    }

    @Override
    protected void toPlainText(StringBuilder builder) {
        builder.append(this.value);
        super.toPlainText(builder);
    }

    @Override
    protected void toLegacyText(StringBuilder builder) {
        addFormat(builder);
        builder.append(this.value);
        super.toLegacyText(builder);
    }

    public String getName() {
        return this.name;
    }

    public String getObjective() {
        return this.objective;
    }

    public String getValue() {
        return this.value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ScoreComponent)) return false;
        final ScoreComponent other = (ScoreComponent) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$objective = this.getObjective();
        final Object other$objective = other.getObjective();
        if (this$objective == null ? other$objective != null : !this$objective.equals(other$objective)) return false;
        final Object this$value = this.getValue();
        final Object other$value = other.getValue();
        return this$value == null ? other$value == null : this$value.equals(other$value);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ScoreComponent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $objective = this.getObjective();
        result = result * PRIME + ($objective == null ? 43 : $objective.hashCode());
        final Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }

    public String toString() {
        return "ScoreComponent(name=" + this.getName() + ", objective=" + this.getObjective() + ", value=" + this.getValue() + ")";
    }
}
