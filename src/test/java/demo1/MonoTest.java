package demo1;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author vicboma
 */
public class MonoTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void never() throws Exception {

        final String expected = "MonoNever";

        Assert.assertEquals(expected,Mono.never().toString());
    }

    @Test
    public void just() throws Exception {

        String hello = "Hello";

        final StringBuilder expected = new StringBuilder(hello);
        final StringBuilder result = new StringBuilder();

        Mono.just(hello)
                .subscribe(result::append);

        Assert.assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void delay() throws Exception {

        String hello = "Hello";

        final StringBuilder expected = new StringBuilder();
        final StringBuilder result = new StringBuilder();

        Mono.just(hello)
                .delayElement(Duration.ofMillis(400))
                .subscribe(result::append);

        Assert.assertEquals(expected.toString(), "");

    }

    @Test
    public void delaySubscribe() throws Exception {


        String hello = "Hello";

        final StringBuilder expected = new StringBuilder(hello);
        final StringBuilder result = new StringBuilder();

        Mono.just(hello)
                .delaySubscription(Duration.ofMillis(200))
                .subscribe(result::append);

        new CountDownLatch(1).await(300, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void and() throws Exception {
        String hello = "Hello";

        final Mono<String> just = Mono.just(hello);
        final Mono<Void> and = just.and(it -> Mono.<String>just(hello));

        final CompletableFuture<String> stringCompletableFuture = just.toFuture();
        stringCompletableFuture
            .thenAcceptAsync(it -> {
                Assert.assertEquals(and.block(),Void.TYPE);
            });

        Assert.assertEquals(hello,stringCompletableFuture.get());
    }

    @Test
    public void as() throws Exception {

        String date = "1986";

        final int result = Mono.just(date)
                .as(it -> Integer.valueOf(it.block()))
                .intValue();

        Assert.assertTrue(1986 == result);
    }

    @Test
    public void cast() throws Exception {

        Number date = 1986;

        Mono.just(date)
                .cast(Integer.class)
                .subscribe(it -> {
                    Assert.assertTrue(1986 == it);
                });
    }

    @Test
    public void create() throws Exception {
        final int expected = 10;

        final Mono<Integer> mono = Mono.<Integer>create(monoSink -> {
            monoSink.success(expected);
        });

        new CountDownLatch(1).await(100, TimeUnit.MILLISECONDS);

        Assert.assertTrue(expected == mono.block());
    }

    @Test
    public void empty() throws Exception {
        String expected = null;
        Mono<String> result = Mono.empty();

        Assert.assertEquals(expected, result.block());
    }

    @Test
    public void justOrEmpty() throws Exception {
        String expected = null;
        Mono<String> result = Mono.justOrEmpty(expected);

        Assert.assertEquals(expected, result.block());
    }

    @Test
    public void first() throws Exception {
        String expected = "Hello";
        Mono<String> result = Mono.first(List.of(
                Mono.just(expected)
                        .delayElement(Duration.ofMillis(500)),
                Mono.just("World")
                        .delayElement(Duration.ofMillis(550)))
        );

        Assert.assertEquals(expected, result.block());

    }

    @Test
    public void fromSupplier() throws Exception {
        final StringBuilder result = new StringBuilder();

        String expected = "MonoSupplier";

        Mono.just("")
            .map( it ->
                    Mono.fromSupplier(
                            () -> Mono.empty()
                    )
            )
            .subscribe(result::append);

        new CountDownLatch(1).await(1000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expected, result.toString());

    }

    @Test
    public void fromCallable() throws Exception {
        final StringBuilder result = new StringBuilder();

        String expected = "MonoCallable";

        Mono.just("")
                .map( it ->
                        Mono.fromCallable(
                                () -> Mono.empty()
                        )
                )
                .subscribe(result::append);

        new CountDownLatch(1).await(1000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void fromRunnable() throws Exception {
        final StringBuilder result = new StringBuilder();

        String expected = "MonoRunnable";

        Mono.just("")
                .map( it ->
                        Mono.fromRunnable(
                                () -> Mono.empty()
                        )
                )
                .subscribe(result::append);

        new CountDownLatch(1).await(1000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void fromCompletable() throws Exception {
        final CompletableFuture<Boolean> expected = CompletableFuture.completedFuture(false);

        final StringBuilder result = new StringBuilder();

        Mono.just("")
                .map( it ->
                        Mono.fromFuture(expected)
                )
                .subscribe(result::append);
        expected.get();

        Assert.assertEquals("MonoCompletionStage", result.toString());
    }


  /*  @Test
    public void zipImpl() throws Exception{

        final String a = "A";
        final String b = "B";
        final String data = "";

        final StringBuilder expected = new StringBuilder().append(a).append(b).append(data);
        final StringBuilder result = new StringBuilder();

        Mono.when(
                Mono.just(a),
                Mono.just(b),
                Mono.justOrEmpty(data)
        ).map(t ->
             new StringBuffer()
                    .append(t.getT1().toString())
                    .append(t.getT2().toString())
                    .append(t.getT3().toString())
                    .toString()
        ).subscribe(
                result::append,
                s -> System.out.println("error: " + s),
                () -> { System.out.println("complete!");
         });

        Assert.assertEquals(expected.toString(), result.toString());
    }
*/
    @Test
    public void zip() throws Exception{

        final String a = "A";
        final String b = "B";
        final String data = "";

        final StringBuilder expected = new StringBuilder().append(a).append(b).append(data);
        final StringBuilder result = new StringBuilder();

        Mono.zip(strings -> {
            final StringBuffer sb = new StringBuffer();
            for (Object string : strings) {
                sb.append((String) string);
            }
            return sb.toString();
        },
                Mono.just(a),
                Mono.just(b),
                Mono.justOrEmpty(data)
        )
         .subscribe(
                result::append,
                s -> System.out.println("error: " + s),
                () -> { System.out.println("complete!");
                });

        Assert.assertEquals(expected.toString(), result.toString());
    }
}
