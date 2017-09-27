package core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class EmitterProcessorTest {

    static final Logger LOG = LoggerFactory.getLogger(EmitterProcessorTest.class);
    final String expected = "Hello World!";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    //========================================================================================

    /**
     *
     * @throws Exception
     */
    @Test
    public void create() throws Exception {
        final String expected = EmitterProcessor.class.getName();
        final EmitterProcessor<String> stream = EmitterProcessor.<String>create();

        Assert.assertEquals(expected, stream.getClass().getName());
    }

    //========================================================================================

    @Test
    public void filter() throws Exception {
        final String filter = "World";

        final String expected1 = "Hello "+filter+"!";
        final String expected2 = "Goodbye "+filter+"!";

        final List<String> expected = List.of(expected1,expected2);
        final List<String> result = new ArrayList<>();

        EmitterProcessor<String> emitter;

        (emitter = EmitterProcessor.create())
                .filter(s -> s.endsWith(filter + "!"))
                .doOnNext(s -> LOG.info("Filtered String {}", s))
                .collectList()
                .subscribe(
                        it -> {
                            LOG.info("Consumer: {}",it);
                            result.addAll(it);
                        },
                        it -> {
                            LOG.error("Error {}", it);
                            Assert.assertTrue(false);
                        },
                        () ->{
                            LOG.info("Completed Consumer");
                        });

        emitter.onNext(expected1);
        emitter.onNext(expected2);
        emitter.onNext("World");
        emitter.onComplete();

        Assert.assertEquals(expected.toString(),result.toString());

    }

    //========================================================================================

    /**
     *
     * @throws Exception
     */
    @Test
    public void nextFuture() throws Exception {
        final EmitterProcessor<String> stream = EmitterProcessor.<String>create();

        stream.doOnNext(s -> LOG.info("Consumed String {}", s))
               .next()
               .toFuture()
               .thenAcceptAsync(it -> {
                    LOG.info("ThenAcceptAsync: {}",it);
                    Assert.assertEquals(expected,it);
               });

        stream.subscribe();
        stream.onNext(expected);
    }

    //========================================================================================

    /**
     *
     * @throws Exception
     */
    @Test
    public void nextSubscribe() throws Exception {
        final EmitterProcessor<String> stream = EmitterProcessor.<String>create();

        stream.doOnEach(s -> LOG.info("doOnEach String {}", s))
               .doOnSubscribe(it -> it.request(Integer.MAX_VALUE))
               .next()
               .subscribe(it -> {
                LOG.info("subscribe: {}",it);
                Assert.assertEquals(expected,it);
            });

        stream.onNext(expected);
    }

    //========================================================================================

    /**
     *
     * @throws Exception
     */
    @Test
    public void collectList() throws Exception {

            final StringBuilder _expected = new StringBuilder("[Hello World!, Hello World!2, Hello World!1]");
            final String expected2 = "Hello World!2";
            final String expected3 = "Hello World!1";

            final EmitterProcessor<String> stream = EmitterProcessor.<String>create();

            stream.doOnNext(s -> LOG.info("doOnNext String {}", s))
                  .doOnComplete(() ->{
                      LOG.info("doOnComplete!!!");
                  } )
                  .collectList()
                  .subscribe(it -> {
                      LOG.info("subscribe: {}",it);
                      Assert.assertEquals(_expected.toString(),it.toString());
                  });

            stream.onNext(expected);
            stream.onNext(expected2);
            stream.onNext(expected3);

            stream.onComplete();
    }
}