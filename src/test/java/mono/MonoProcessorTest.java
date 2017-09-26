package mono;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import javax.swing.*;
import java.time.Duration;

public class MonoProcessorTest {

    static final Logger LOG = LoggerFactory.getLogger(MonoProcessorTest.class);
    final String expected = "Hello World!";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void monoBlock() throws Exception {

            SwingUtilities.invokeAndWait( ()->{
                // Deferred is the publisher, Promise the consumer
                final MonoProcessor<String> promise = MonoProcessor.create();

                final Mono<String> result =  promise
                        .doOnSubscribe(it -> LOG.info("onSubscribed: {}", it) )
                        .doOnSuccess(it ->  {
                            LOG.info("onSuccess - Promise completed: {}", it);
                            Assert.assertEquals(expected,it);
                        })
                        .doOnNext(it ->      LOG.info("onNext: {}",it))
                        .doOnEach(it ->      LOG.info("onEach: {}",it))
                        .doOnTerminate(() -> LOG.info("onTerminate!!!"))
                        .doOnError(t ->      LOG.error("onError: {} - {} ",t.getMessage(), t));

                promise.subscribe();

                promise.onNext(expected);

                String s = result.block(Duration.ofMillis(1000));
                LOG.info("Result Block={}", s);

                Assert.assertEquals(expected,s);

            });
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void monoBlockRefactor() throws Exception {

        SwingUtilities.invokeAndWait( ()->{
            // Deferred is the publisher, Promise the consumer
            final MonoProcessor<String> promise = MonoProcessor.create();
            promise
                .doOnSubscribe(it -> {
                    LOG.info("onSubscribed: {}", it);
                    it.request(Integer.MAX_VALUE); // only [min 0 - max 1] (MonoProcessor)
                } )
                .doOnSuccess(it ->  {
                    LOG.info("onSuccess - Promise completed: {}", it);
                    Assert.assertEquals(expected,it);
                })
                .doOnNext( it ->     LOG.info("onNext: {}", it))
                .doOnRequest(it ->   LOG.info("onRequest: {}", it))
                .doOnEach(it ->      LOG.info("onEach: {}",it))
                .doOnTerminate(() -> LOG.info("onTerminate!!!"))
                .doOnError(t ->      LOG.error("onError: {} - {} ",t.getMessage(), t))
                .subscribe(it -> {
                    Assert.assertEquals(expected,it);
                });


            promise.onNext(expected);

            promise.onNext("Hello World! 2"); // onNextDropped
            promise.onNext("Hello World! 3");  //onNextDropped
        });
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void monoToFuture() throws Exception {

        SwingUtilities.invokeAndWait(() -> {
            // Deferred is the publisher, Promise the consumer
            final MonoProcessor<String> promise = MonoProcessor.create();

            final Mono<String> result = promise
                    .doOnSubscribe(it -> LOG.info("onSubscribed: {}", it))
                    .doOnSuccess(it -> {
                        LOG.info("onSuccess - Promise completed: {}", it);
                        Assert.assertEquals(expected, it);
                    })
                    .doOnNext(it -> LOG.info("onNext: {}", it))
                    .doOnEach(it -> LOG.info("onEach: {}", it))
                    .doOnTerminate(() -> LOG.info("onTerminate!!!"))
                    .doOnError(t -> LOG.error("onError: {} - {} ", t.getMessage(), t));

            promise.subscribe();

            promise.onNext("Hello World!");

            result.toFuture();
        });
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void hasDownstreams() throws Exception {

        SwingUtilities.invokeAndWait( ()->{
            // Deferred is the publisher, Promise the consumer
            final MonoProcessor<String> promise = MonoProcessor.create();
            promise.subscribe(it -> {
                Assert.assertEquals(expected,it);
            });

            promise.onNext(expected);

            Assert.assertFalse(promise.hasDownstreams());

        });
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void downstreamCount() throws Exception {

        SwingUtilities.invokeAndWait( ()->{

            final MonoProcessor<String> promise = MonoProcessor.create();

            promise.subscribe(it -> {
                        Assert.assertEquals(expected,it);
                    });

            promise.onNext(expected);

            Assert.assertTrue(promise.downstreamCount() == 0);

        });
    }

}