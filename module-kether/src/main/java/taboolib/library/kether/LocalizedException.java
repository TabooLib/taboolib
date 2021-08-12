package taboolib.library.kether;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalizedException extends RuntimeException {

    private final String node;
    private final Object[] params;

    public LocalizedException(String node, Object[] params) {
        this.node = node;
        this.params = params;
    }

    public String getNode() {
        return node;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public String getLocalizedMessage() {
        return QuestService.instance().getLocalizedText(node, params);
    }

    public Stream<LocalizedException> stream() {
        return Stream.of(this);
    }

    public LocalizedException then(LocalizedException e) {
        return batch(this, e);
    }

    public static LocalizedException of(String node, Object... params) {
        return new LocalizedException(node, params);
    }

    public static Supplier<LocalizedException> supply(String node, Object... params) {
        return () -> of(node, params);
    }

    public static LocalizedException batch(LocalizedException... exceptions) {
        return new Concat(exceptions);
    }

    private static class Concat extends LocalizedException {

        private final LocalizedException[] exceptions;

        public Concat(LocalizedException... exceptions) {
            super(exceptions[0].node, exceptions[0].params);
            this.exceptions = exceptions;
        }

        @Override
        public Stream<LocalizedException> stream() {
            return Arrays.stream(exceptions);
        }

        @Override
        public String getLocalizedMessage() {
            return stream()
                .map(LocalizedException::getLocalizedMessage)
                .collect(Collectors.joining("\n"));
        }

        @Override
        public LocalizedException then(LocalizedException e) {
            LocalizedException[] arr = new LocalizedException[exceptions.length + 1];
            System.arraycopy(this.exceptions, 0, arr, 0, this.exceptions.length);
            arr[exceptions.length] = e;
            return new Concat(arr);
        }
    }
}
