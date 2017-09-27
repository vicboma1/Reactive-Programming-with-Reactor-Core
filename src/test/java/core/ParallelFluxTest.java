package core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author vicboma
 */
public class ParallelFluxTest {

    static final Logger LOG = LoggerFactory.getLogger(ParallelFluxTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    //========================================================================================

    @Test
    public void parallelWithOneThread() throws Exception {

        final StringBuilder expected = new StringBuilder().
               append("main -> 1").
               append("main -> 2").
               append("main -> 3").
               append("main -> 4").
               append("main -> 5").
               append("main -> 6").
               append("main -> 7").
               append("main -> 8").
               append("main -> 9").
               append("main -> 10");

        final StringBuilder result = new StringBuilder();

        Flux.range(1, 10)
                .parallel(2)
                .subscribe(
                        i -> {
                            String x = Thread.currentThread().getName() + " -> " + i;
                            result.append(x);
                            System.out.println(x);
                        },
                    System.err::println,
                    () -> {}
                );

        Assert.assertEquals(expected.toString(),result.toString());
    }

    //========================================================================================

    @Test
    public void parallelWithSchedulers() throws Exception {
        final SortedSet<Integer> resultThread = new TreeSet<>();
        final Set<Integer>  resultThreadSorted = Collections.synchronizedSet(resultThread);

        final SortedSet<Integer> result = new TreeSet<>();
        final Set<Integer> resultSynchronize = Collections.synchronizedSet(result);

        Flux.<Integer>range(1, 10)
                .parallel(3)
                .runOn(Schedulers.parallel())
                .subscribe(
                        i -> {
                            resultThreadSorted.add(Integer.valueOf(Thread.currentThread().getName()));
                            resultSynchronize.add(i);
                            System.out.println(Thread.currentThread().getName() + " -> " + i);
                        },
                        System.err::println
                );

        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);

    }

    //========================================================================================

    @Test
    public void elasticWithSchedulers() throws Exception {
        final List<Integer> expected = List.of(1,2,3,4,5,6,7,8,9,10,11,12,13);
        final List<Integer> expectedThread = List.of(1,2,3,4,5);

        final SortedSet<Integer> resultThread = new TreeSet<>();
        final Set<Integer>  resultThreadSorted = Collections.synchronizedSet(resultThread);

        final SortedSet<Integer> result = new TreeSet<>();
        final Set<Integer> resultSynchronize = Collections.synchronizedSet(result);

        Flux.<Integer>range(1, 13)
                .parallel(5)
                .runOn(Schedulers.elastic())
                .subscribe(
                        i -> {
                            resultThreadSorted.add(Integer.valueOf(Thread.currentThread().getName().split("-")[1]));
                            resultSynchronize.add(i);
                            System.out.println(Thread.currentThread().getName() + " -> " + i);
                        },
                        System.err::println
                );

        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expectedThread.size(), resultThreadSorted.size());
        Assert.assertEquals(expected.toString(), resultSynchronize.toString());
    }

    //========================================================================================

    @Test
    public void fromExecutorsWithSchedulers() throws Exception {
        final String expected = "pool";
        final List<Integer> expectedThread = List.of(1,2,3,4,5,6,7,8,9,10);

        final SortedSet<String> resultThread = new TreeSet<>();
        final Set<String>  resultThreadSorted = Collections.synchronizedSet(resultThread);

        final SortedSet<Integer> result = new TreeSet<>();
        final Set<Integer> resultSynchronize = Collections.synchronizedSet(result);

        Flux.<Integer>range(1, 10)
                .parallel(10)
                .runOn(Schedulers.fromExecutor(Executors.newFixedThreadPool(1),true))
                .subscribe(
                        i -> {
                            resultThreadSorted.add(Thread.currentThread().getName());
                            resultSynchronize.add(i);
                            System.out.println(Thread.currentThread().getName() + " -> " + i);
                        },
                        System.err::println
                );

        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(resultThreadSorted.stream().filter(it -> it.substring(0,4).equals(expected)).count(), 1);
        Assert.assertEquals(expectedThread.toString(), resultSynchronize.toString());
        Assert.assertEquals(expectedThread.size(), resultSynchronize.size());

    }

    //========================================================================================

    @Test
    public void fromExecutorServiceWithSchedulers() throws Exception {
        final List<String> expected = List.of("ForkJoinPool-1-worker");
        final List<Integer> expectedThread = List.of(1,2,3,4,5,6,7,8,9,10);

        final SortedSet<String> resultThread = new TreeSet<>();
        final Set<String>  resultThreadSorted = Collections.synchronizedSet(resultThread);

        final SortedSet<Integer> result = new TreeSet<>();
        final Set<Integer> resultSynchronize = Collections.synchronizedSet(result);

        Flux.<Integer>range(1, 10)
                .parallel()
                .runOn(Schedulers.fromExecutorService(Executors.newWorkStealingPool(5)))
                .subscribe(
                        i -> {
                            resultThreadSorted.add(Thread.currentThread().getName().substring(0,21));
                            resultSynchronize.add(i);
                            System.out.println(Thread.currentThread().getName() + " -> " + i);
                        },
                        System.err::println
                );

        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expected.toString(), resultThreadSorted.toString());
        Assert.assertEquals(expectedThread.toString(), resultSynchronize.toString());
        Assert.assertEquals(expected.size(), resultThreadSorted.size());
        Assert.assertEquals(expectedThread.size(), resultSynchronize.size());

    }

    //========================================================================================

    @Test
    public void groups() throws Exception {

        final StringBuilder expectedThread = new StringBuilder().
                append("0a").
                append("1b").
                append("2c").
                append("3d").
                append("4e");

        final StringBuilder result = new StringBuilder();

        Flux.just("a","b","c","d","e")
                .parallel(5, 256)
                .runOn(Schedulers.parallel())
                .groups()
                .toIterable()
                .forEach(it -> result.append(it.key()+""+it.single().block()));

        Assert.assertEquals(expectedThread.toString(), result.toString());

    }

    //========================================================================================

    //subscribeOn -> Subscribes to this ParallelFlux by providing an onNext, onError, onComplete and onSubscribe callback and triggers the execution chain for all 'rails'.
    //publishOn  -> Run onNext, onComplete and onError on a supplied Scheduler Scheduler.Worker.

    @Test
    public void completableAsync() throws Exception {

        final CompletableFuture lock  =  new CompletableFuture();
        final List<CompletableFuture> promises =  new LinkedList();
        final List<String>  expected = List.of("BLUE 1","WHITE 1","RED 1","BLUE 2","WHITE 2","RED 2");
        final List<String> result = new ArrayList<>();

        final Disposable disposable = Flux.just("blue 1", "white 1", "red 1","blue 2", "white 2", "red 2")
                .doOnEach(it -> LOG.info("doOnEach: " + it))
                .doOnNext(it -> LOG.info("doOnNext: " + it))
                .doOnSubscribe(it -> LOG.info("doOnSubscribe: " + it))
                //.subscribeOn(Schedulers.parallel())
                //.publishOn(Schedulers.parallel())
                .parallel()
                .runOn(Schedulers.parallel())
                .map(String::toUpperCase)
                .subscribe(
                        value ->  {
                            promises.add(
                                    CompletableFuture.runAsync(() ->{
                                        result.add(value);
                                        LOG.info("Consumed : {}", value);
                                    })
                            );
                        },
                        throwable -> LOG.error("Error : {}",throwable),
                        () -> {

                            LOG.info("Completed consumer!!!");

                            try {
                                CompletableFuture.allOf(promises.toArray(new CompletableFuture[promises.size()]))
                                        .get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            Assert.assertEquals(promises.size() ,expected.size());

                            for (int i = 0; i < expected.size(); i++) {
                                Assert.assertTrue(result.contains(expected.get(i)));
                            }

                            lock.complete(null);
                        }
                );


        lock.get();

        if(!disposable.isDisposed())
            disposable.dispose();
    }

}


