package taboolib.library.kether;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Parser<T> implements App<Parser.Mu, T> {

    public static final class Mu implements K1 {
    }

    private static <T> Parser<T> unbox(App<Parser.Mu, T> box) {
        return (Parser<T>) box;
    }

    @FunctionalInterface
    public interface Action<T> {

        CompletableFuture<T> run(@NotNull QuestContext.Frame frame);

        static <T> Action<T> point(T value) {
            return frame -> CompletableFuture.completedFuture(value);
        }
    }

    public final Function<QuestReader, Action<T>> reader;

    private Parser(Function<QuestReader, Action<T>> reader) {
        this.reader = reader;
    }

    public Parser<T> orElse(Parser<T> other) {
        return new Parser<>(r -> {
            r.mark();
            try {
                return reader.apply(r);
            } catch (Exception e) {
                r.reset();
                return other.reader.apply(r);
            }
        });
    }

    public <B> Parser<Either<T, B>> either(Parser<B> parser) {
        return map(Either::<T, B>left).orElse(parser.map(Either::right));
    }

    public Parser<Optional<T>> optional() {
        return map(Optional::ofNullable).orElse(point(Optional.empty()));
    }

    @SuppressWarnings("unchecked")
    public Parser<List<T>> listOf() {
        return frame(r -> {
            r.expect("[");
            ArrayList<Action<T>> list = new ArrayList<>();
            while (r.hasNext() && r.peek() != ']') {
                list.add(reader.apply(r));
            }
            r.expect("]");
            list.trimToSize();
            return frame -> {
                CompletableFuture<T>[] futures = (CompletableFuture<T>[]) list.stream().map(it -> it.run(frame)).toArray(CompletableFuture<?>[]::new);
                return CompletableFuture.allOf(futures)
                        .thenApply(it -> Arrays.stream(futures).map(CompletableFuture::join).collect(Collectors.toList()));
            };
        });
    }

    public <R> Parser<R> map(Function<T, R> func) {
        return unbox(instance().ap(func, this));
    }

    public <B, R> Parser<R> fold(Parser<B> fb, BiFunction<T, B, R> func) {
        return unbox(instance().apply2(func, this, fb));
    }

    public <B, C, R> Parser<R> fold(Parser<B> fb, Parser<C> fc, Function3<T, B, C, R> func) {
        return unbox(instance().apply3(func, this, fb, fc));
    }

    public static <T> Parser<T> point(T value) {
        return new Parser<>(r -> Action.point(value));
    }

    public static <T> Parser<T> of(Function<QuestReader, T> parser) {
        return new Parser<>(r -> Action.point(parser.apply(r)));
    }

    public static <T> Parser<T> frame(Function<QuestReader, Action<T>> parser) {
        return new Parser<>(parser);
    }

    public static <A> QuestActionParser create(final Function<Instance, ? extends App<Mu, Action<A>>> builder) {
        return build(builder.apply(instance()));
    }

    public static <A> QuestActionParser build(App<Mu, Action<A>> fa) {
        Function<QuestReader, Action<Action<A>>> f = unbox(fa).reader;
        return new QuestActionParser() {
            @Override
            public <T> QuestAction<T> resolve(@NotNull QuestReader resolver) {
                Action<Action<A>> action = f.apply(resolver);
                return new QuestAction<T>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public CompletableFuture<T> process(@NotNull QuestContext.Frame frame) {
                        return action.run(frame).thenCompose(it -> (CompletableFuture<T>) it.run(frame));
                    }
                };
            }
        };
    }

    public static Instance instance() {
        return Instance.INSTANCE;
    }

    public enum Instance implements Applicative<Mu, Instance.Mu> {
        INSTANCE;

        private static final class Mu implements Applicative.Mu {
        }

        @Override
        public <A> App<Parser.Mu, A> point(A a) {
            return new Parser<>(it -> Action.point(a));
        }

        @Override
        public <A, R> Function<App<Parser.Mu, A>, App<Parser.Mu, R>> lift1(App<Parser.Mu, Function<A, R>> function) {
            Parser<Function<A, R>> f = unbox(function);
            return a -> {
                Parser<A> fa = unbox(a);
                return new Parser<>(r -> {
                    Action<Function<A, R>> af = f.reader.apply(r);
                    Action<A> aa = fa.reader.apply(r);
                    return frame -> {
                        return af.run(frame).thenCompose(f1 -> aa.run(frame).thenApply(f1));
                    };
                });
            };
        }

        @Override
        public <A, R> App<Parser.Mu, R> map(Function<? super A, ? extends R> func, App<Parser.Mu, A> ts) {
            Function<QuestReader, Action<A>> function = unbox(ts).reader;
            return new Parser<>(r -> {
                Action<A> a = function.apply(r);
                return frame -> a.run(frame).thenApply(func);
            });
        }

        @Override
        public <A, B, R> App<Parser.Mu, R> ap2(App<Parser.Mu, BiFunction<A, B, R>> func, App<Parser.Mu, A> a, App<Parser.Mu, B> b) {
            Parser<BiFunction<A, B, R>> f = unbox(func);
            Parser<A> fa = unbox(a);
            Parser<B> fb = unbox(b);
            return new Parser<>(r -> {
                Action<BiFunction<A, B, R>> af = f.reader.apply(r);
                Action<A> aa = fa.reader.apply(r);
                Action<B> ab = fb.reader.apply(r);
                return frame -> af.run(frame).thenCompose(
                        f1 -> aa.run(frame).thenCompose(
                                f2 -> ab.run(frame).thenApply(
                                        f3 -> f1.apply(f2, f3))
                        )
                );
            });
        }

        @Override
        public <T1, T2, T3, R> App<Parser.Mu, R> ap3(App<Parser.Mu, Function3<T1, T2, T3, R>> func, App<Parser.Mu, T1> t1, App<Parser.Mu, T2> t2, App<Parser.Mu, T3> t3) {
            Parser<Function3<T1, T2, T3, R>> f = unbox(func);
            Parser<T1> fa = unbox(t1);
            Parser<T2> fb = unbox(t2);
            Parser<T3> fc = unbox(t3);
            return new Parser<>(r -> {
                Action<Function3<T1, T2, T3, R>> af = f.reader.apply(r);
                Action<T1> aa = fa.reader.apply(r);
                Action<T2> ab = fb.reader.apply(r);
                Action<T3> ac = fc.reader.apply(r);
                return frame -> af.run(frame).thenCompose(
                        f1 -> aa.run(frame).thenCompose(
                                f2 -> ab.run(frame).thenCompose(
                                        f3 -> ac.run(frame).thenApply(
                                                f4 -> f1.apply(f2, f3, f4)
                                        )
                                )
                        )
                );
            });
        }

        @Override
        public <T1, T2, T3, T4, R> App<Parser.Mu, R> ap4(App<Parser.Mu, Function4<T1, T2, T3, T4, R>> func, App<Parser.Mu, T1> t1, App<Parser.Mu, T2> t2, App<Parser.Mu, T3> t3, App<Parser.Mu, T4> t4) {
            // holy... 如果有 for expression 和 higher kind 的话...
            Parser<Function4<T1, T2, T3, T4, R>> f = unbox(func);
            Parser<T1> fa = unbox(t1);
            Parser<T2> fb = unbox(t2);
            Parser<T3> fc = unbox(t3);
            Parser<T4> fd = unbox(t4);
            return new Parser<>(r -> {
                Action<Function4<T1, T2, T3, T4, R>> af = f.reader.apply(r);
                Action<T1> aa = fa.reader.apply(r);
                Action<T2> ab = fb.reader.apply(r);
                Action<T3> ac = fc.reader.apply(r);
                Action<T4> ad = fd.reader.apply(r);
                return frame -> af.run(frame).thenCompose(
                        f1 -> aa.run(frame).thenCompose(
                                f2 -> ab.run(frame).thenCompose(
                                        f3 -> ac.run(frame).thenCompose(
                                                f4 -> ad.run(frame).thenApply(
                                                        f5 -> f1.apply(f2, f3, f4, f5)
                                                )
                                        )
                                )
                        )
                );
            });
        }
    }
}
