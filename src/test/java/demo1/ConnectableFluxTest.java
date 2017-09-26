package demo1;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.util.List;

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

        final StringBuilder result = new StringBuilder().append(1).append(1).append(1).append(2).append(2).append(2).append(3).append(3).append(3);

        final StringBuilder expected = new StringBuilder();

        final Flux<Integer> source =
                Flux.range(1, 3)
                .doOnSubscribe(s -> {
                    System.out.println("subscribed to source ");
                })
                .doOnNext(expected::append)
                .doOnEach(it -> System.out.println("doOnEach: "+it)) //all description operations
                .doOnError(System.err::println)
                .doFinally(it -> System.out.println("doFinally: "+it));


        final ConnectableFlux<Integer> co = source.publish();

        co.subscribe(
                expected::append, // out
                System.err::println,  // err
                () -> {}  //completed callback
        );

        co.subscribe(
                expected::append, // out
                System.err::println,  // err
                () -> {}  //completed callback
        );

        System.out.println("done subscribing");

        Thread.sleep(500);

        System.out.println("will now connect");

        co.connect();

        Assert.assertEquals(expected.toString(),result.toString());

    }

    @Test
    public void autoConnect() throws Exception {

    }
}
