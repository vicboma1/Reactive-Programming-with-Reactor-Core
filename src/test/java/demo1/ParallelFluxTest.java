package demo1;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author vicboma
 */
public class ParallelFluxTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

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

    @Test
    public void fromExecutorsWithSchedulers() throws Exception {
        final List<String> expected = List.of("pool-1-thread-1");
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

        Assert.assertEquals(expected.toString(), resultThreadSorted.toString());
        Assert.assertEquals(expectedThread.toString(), resultSynchronize.toString());
        Assert.assertEquals(expected.size(), resultThreadSorted.size());
        Assert.assertEquals(expectedThread.size(), resultSynchronize.size());

    }

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
}


