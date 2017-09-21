package demo1;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MonoTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void just() throws Exception {

        String hello = "Hello";

        final StringBuilder expected = new StringBuilder(hello);
        final StringBuilder result = new StringBuilder();

        Mono.just(hello)
            .subscribe(result::append);

        Assert.assertEquals(expected.toString(),result.toString());
    }

    @Test
    public void delay() throws Exception {

        String hello = "Hello";

        final StringBuilder expected = new StringBuilder();
        final StringBuilder result = new StringBuilder();

        Mono.just(hello)
             .delayElement(Duration.ofMillis(400))
             .subscribe(result::append);

        Assert.assertEquals(expected.toString(),"");

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

        Assert.assertEquals(expected.toString(),result.toString());
    }

    @Test
    public void and() throws Exception {

        String hello = "Hello";

        final StringBuilder result = new StringBuilder();

        Mono.just(hello)
             .and(it -> Mono.just(it))
             .subscribe(result::append);

        Assert.assertEquals("[Hello,Hello]",result.toString());
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
        new CountDownLatch(1).await(50, TimeUnit.MILLISECONDS);

        Assert.assertTrue(expected == mono.block());

    }

}