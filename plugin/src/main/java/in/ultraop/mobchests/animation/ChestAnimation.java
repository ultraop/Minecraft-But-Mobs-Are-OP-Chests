package in.ultraop.mobchests.animation;

/** A short-lived visual chest animation composed of one or more display entities. */
public interface ChestAnimation {
    boolean isValid();
    void open(int durationTicks);
    void close(int durationTicks);
    void remove();
}
