package core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

/**
 * @author vicboma
 */
public class ConnectableFluxTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void connect() throws Exception {
        final String done_subscribing = "done subscribing";
        final String will_now_connect = "will now connect";
        final String subscribed_to_source_ = "subscribed to source ";

        final StringBuilder expected = new StringBuilder()
                .append(done_subscribing)
                .append(will_now_connect)
                .append(subscribed_to_source_)
                .append(1).append(1).append(1)
                .append(2).append(2).append(2)
                .append(3).append(3).append(3);

        final StringBuilder result = new StringBuilder();

        final Flux<Integer> source =
                Flux.range(1, 3)
                .doOnSubscribe(s -> {
                    result.append(subscribed_to_source_);
                    System.out.println(subscribed_to_source_);
                })
                .doOnNext(result::append)
                .doOnEach(it -> System.out.println("doOnEach: "+it)) //all description operations
                .doOnError(System.err::println)
                .doFinally(it -> System.out.println("doFinally: "+it));


        final ConnectableFlux<Integer> co = source.publish();

        co.subscribe(
                result::append, // out
                System.err::println,  // err
                () -> {}  //completed callback
        );

        co.subscribe(
                result::append, // out
                System.err::println,  // err
                () -> {}  //completed callback
        );

        result.append(done_subscribing);
        System.out.println(done_subscribing);

        Thread.sleep(500);

        result.append(will_now_connect);
        System.out.println(will_now_connect);

        co.connect();

        Assert.assertEquals(expected.toString(),result.toString());

    }

    @Test
    public void autoConnect() throws Exception {
        final String subscribed_to_source_ = "subscribed to source ";
        final String subscribed_first = "subscribed first";
        final String subscribing_second = "subscribing second";

        final StringBuilder expected = new StringBuilder()
                .append(subscribed_first)
                .append(subscribing_second)
                .append(subscribed_to_source_)
                .append(1).append(1).append(1)
                .append(2).append(2).append(2)
                .append(3).append(3).append(3);

        final StringBuilder result = new StringBuilder();

        final Flux<Integer> source =
                Flux.range(1, 3)
                        .doOnSubscribe(s -> {
                            result.append(subscribed_to_source_);
                        })
                        .doOnNext(result::append)
                        .doOnEach(it -> System.out.println("doOnEach: "+it)) //all description operations
                        .doOnError(System.err::println)
                        .doFinally(it -> System.out.println("doFinally: "+it));

        final Flux<Integer> autoCo = source.publish().autoConnect(2);

        autoCo.subscribe(
                result::append, // out
                System.err::println,  // err
                () -> {}  //completed callback
        );

        result.append(subscribed_first);
        System.out.println(subscribed_first);

        Thread.sleep(500);

        result.append(subscribing_second);
        System.out.println(subscribing_second);

        autoCo.subscribe(
                result::append, // out
                System.err::println,  // err
                () -> {}  //completed callback
        );

        Assert.assertEquals(expected.toString(),result.toString());

    }

    @Test
    public void autoConnectMinSubscribed() throws Exception {
        final String subscribed_to_source_ = "subscribed to source ";
        final String subscribed_first = "subscribed first";
        final String subscribing_second = "subscribing second";

        final StringBuilder expected = new StringBuilder()
                .append(subscribed_first)
                .append(subscribing_second);

        final StringBuilder result = new StringBuilder();

        final Flux<Integer> source =
                Flux.range(1, 3)
                        .doOnSubscribe(s -> {
                            result.append(subscribed_to_source_);
                        })
                        .doOnNext(result::append)
                        .doOnEach(it -> System.out.println("doOnEach: "+it)) //all description operations
                        .doOnError(System.err::println)
                        .doFinally(it -> System.out.println("doFinally: "+it));

        final Flux<Integer> autoCo = source.publish().autoConnect(4);

        autoCo.subscribe(
                result::append, // out
                System.err::println,  // err
                () -> {}  //completed callback
        );

        result.append(subscribed_first);
        System.out.println(subscribed_first);

        Thread.sleep(500);

        result.append(subscribing_second);
        System.out.println(subscribing_second);

        autoCo.subscribe(
                result::append, // out
                System.err::println,  // err
                () -> {}  //completed callback
        );

        Assert.assertEquals(expected.toString(),result.toString());

    }
}
