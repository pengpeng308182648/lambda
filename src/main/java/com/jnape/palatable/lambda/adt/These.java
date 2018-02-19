package com.jnape.palatable.lambda.adt;

import com.jnape.palatable.lambda.adt.coproduct.CoProduct2;
import com.jnape.palatable.lambda.adt.coproduct.CoProduct3;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.Bifunctor;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.traversable.Traversable;

import java.util.Objects;
import java.util.function.Function;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;

/**
 * The coproduct of a coproduct (<code>{@link CoProduct2}&lt;A, B&gt;</code>) and its product (<code>{@link
 * Tuple2}&lt;A, B&gt;</code>), represented as a <code>{@link CoProduct3}&lt;A, B, {@link Tuple2}&lt;A,
 * B&gt;&gt;</code>.
 *
 * @param <A> the first possible type
 * @param <B> the second possible type
 */
public abstract class These<A, B> implements CoProduct3<A, B, Tuple2<A, B>, These<A, B>>, Monad<B, These<A, ?>>, Bifunctor<A, B, These<?, ?>>, Traversable<B, These<A, ?>> {

    private These() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <C, D> These<C, D> biMap(Function<? super A, ? extends C> lFn,
                                          Function<? super B, ? extends D> rFn) {
        return match(a -> a(lFn.apply(a)), b -> b(rFn.apply(b)), into((a, b) -> both(lFn.apply(a), rFn.apply(b))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <C> These<A, C> flatMap(Function<? super B, ? extends Monad<C, These<A, ?>>> f) {
        return match(These::a, b -> f.apply(b).coerce(), into((a, b) -> f.apply(b).<These<A, C>>coerce().biMapL(constantly(a))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <C> These<A, C> pure(C c) {
        return match(a -> both(a, c), b -> b(c), into((a, b) -> both(a, c)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C, App extends Applicative> Applicative<These<A, C>, App> traverse(
            Function<? super B, ? extends Applicative<C, App>> fn,
            Function<? super Traversable<C, These<A, ?>>, ? extends Applicative<? extends Traversable<C, These<A, ?>>, App>> pure) {
        return match(a -> pure.apply(a(a)).fmap(x -> (These<A, C>) x),
                     b -> fn.apply(b).fmap(this::pure),
                     into((a, b) -> fn.apply(b).fmap(c -> both(a, c))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <Z> These<Z, B> biMapL(Function<? super A, ? extends Z> fn) {
        return (These<Z, B>) Bifunctor.super.biMapL(fn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <C> These<A, C> biMapR(Function<? super B, ? extends C> fn) {
        return (These<A, C>) Bifunctor.super.biMapR(fn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <C> These<A, C> fmap(Function<? super B, ? extends C> fn) {
        return Monad.super.<C>fmap(fn).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <C> These<A, C> zip(Applicative<Function<? super B, ? extends C>, These<A, ?>> appFn) {
        return Monad.super.zip(appFn).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <C> These<A, C> discardL(Applicative<C, These<A, ?>> appB) {
        return Monad.super.discardL(appB).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <C> These<A, B> discardR(Applicative<C, These<A, ?>> appB) {
        return Monad.super.discardR(appB).coerce();
    }

    /**
     * Static factory method for wrapping a value of type <code>A</code> in a {@link These}.
     *
     * @param a   the value
     * @param <A> the first possible type
     * @param <B> the second possible type
     * @return the wrapped value as a <code>{@link These}&lt;A,B&gt;</code>
     */
    public static <A, B> These<A, B> a(A a) {
        return new _A<>(a);
    }

    /**
     * Static factory method for wrapping a value of type <code>B</code> in a {@link These}.
     *
     * @param b   the value
     * @param <A> the first possible type
     * @param <B> the second possible type
     * @return the wrapped value as a <code>{@link These}&lt;A,B&gt;</code>
     */
    public static <A, B> These<A, B> b(B b) {
        return new _B<>(b);
    }

    /**
     * Static factory method for wrapping a value of type <code>A</code> and a value of type <code>B</code> in a {@link
     * These}.
     *
     * @param a   the first value
     * @param b   the second value
     * @param <A> the first possible type
     * @param <B> the second possible type
     * @return the wrapped values as a <code>{@link These}&lt;A,B&gt;</code>
     */
    public static <A, B> These<A, B> both(A a, B b) {
        return new Both<>(tuple(a, b));
    }

    private static final class _A<A, B> extends These<A, B> {

        private final A a;

        private _A(A a) {
            this.a = a;
        }

        @Override
        public <R> R match(Function<? super A, ? extends R> aFn, Function<? super B, ? extends R> bFn,
                           Function<? super Tuple2<A, B>, ? extends R> cFn) {
            return aFn.apply(a);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof These._A && Objects.equals(a, ((_A) other).a);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a);
        }

        @Override
        public String toString() {
            return "These{a=" + a + '}';
        }
    }

    private static final class _B<A, B> extends These<A, B> {
        private final B b;

        private _B(B b) {
            this.b = b;
        }

        @Override
        public <R> R match(Function<? super A, ? extends R> aFn, Function<? super B, ? extends R> bFn,
                           Function<? super Tuple2<A, B>, ? extends R> cFn) {
            return bFn.apply(b);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof These._B && Objects.equals(b, ((_B) other).b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(b);
        }

        @Override
        public String toString() {
            return "These{b=" + b + '}';
        }
    }

    private static final class Both<A, B> extends These<A, B> {
        private final Tuple2<A, B> both;

        private Both(Tuple2<A, B> tuple) {
            this.both = tuple;
        }

        @Override
        public <R> R match(Function<? super A, ? extends R> aFn, Function<? super B, ? extends R> bFn,
                           Function<? super Tuple2<A, B>, ? extends R> cFn) {
            return cFn.apply(both);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Both && Objects.equals(both, ((Both) other).both);
        }

        @Override
        public int hashCode() {
            return Objects.hash(both);
        }

        @Override
        public String toString() {
            return "These{both=" + both + '}';
        }
    }
}