package core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author vicboma
 */
public class FluxTest {

    static final Logger LOG = LoggerFactory.getLogger(FluxTest.class);

    final String the = "the";
    final String words1 = "word";
    final String brown = "brown";
    final String fox = "fox";
    final String jumped = "jumped";
    final String over = "over";
    final String lazy = "lazy";
    final String dog = "dog";

    private List<String> words;

    @Before
    public void setUp() throws Exception {
        words = List.of(
                the,
                words1,
                brown,
                fox,
                jumped,
                over,
                the,
                lazy,
                dog
        );
    }

    @After
    public void tearDown() throws Exception {
        words = null;
    }

    //========================================================================================

    @Test
    public void never() throws Exception {
        final String expected = "FluxNever";
        Assert.assertEquals(expected, Flux.never().toString());
    }

    //========================================================================================

    @Test
    public void just() throws Exception {

        String hello = "Hello";
        String world = "World";

        final StringBuilder expected = new StringBuilder(hello)
                .append(world);

        final StringBuilder result = new StringBuilder();

        Flux.just(hello, world)
                .subscribe(result::append);

        Assert.assertEquals(expected.toString(), result.toString());
    }

    //========================================================================================

    @Test
    public void fromIterable() throws Exception {

        final StringBuilder result = new StringBuilder();

        Mono.just("[")
                .concatWith(
                        Flux.fromIterable(words)
                                .filter(word -> !word.equals(dog))
                                .flatMap(word -> Flux.just(word.concat(", ")))
                )
                .concatWith(Flux.just(dog, "]"))
                .subscribe(result::append);


        Assert.assertEquals(words.toString(), result.toString());
    }

    //========================================================================================

    @Test
    public void fromIterableWithThrowableWithCompleteConsume() throws Exception {

        final StringBuilder result = new StringBuilder();

        Mono.just("[")
                .concatWith(
                        Flux.fromIterable(words)
                                .filter(word -> !word.equals(dog))
                                .flatMap(word -> Flux.just(word.concat(", ")))
                )
                .concatWith(Flux.just(dog, "]"))
                .subscribe(
                        result::append,
                        throwable -> System.err.println(throwable.getMessage()),
                        () -> {
                            Assert.assertEquals(words.toString(), result.toString());
                        });
    }

    //========================================================================================

    @Test
    public void findingMissingLetterFromArray() {
        final List<String> result = List.of(
                " 1.a",
                " 2.b",
                " 3.d",
                " 4.e",
                " 5.f",
                " 6.g",
                " 7.h",
                " 8.j",
                " 9.l",
                "10.m",
                "11.n",
                "12.o",
                "13.p",
                "14.r",
                "15.t",
                "16.u",
                "17.v",
                "18.w",
                "19.x",
                "20.y",
                "21.z");

        final List<String> expected = new ArrayList();

        Flux.fromIterable(words)
                .flatMap(word -> Flux.fromArray(word.split("")))
                .distinct()
                .sort()
                .zipWith(Flux.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d.%s", count, string))
                .subscribe(expected::add);

        result.stream()
                .forEach(expected::remove);

        Assert.assertTrue(expected.isEmpty());
    }

    //========================================================================================

    @Test
    public void restoringMissingLetterIterable() {
        final List<String> result = List.of(
                " 1.a",
                " 2.b",
                " 3.d",
                " 4.e",
                " 5.f",
                " 6.g",
                " 7.h",
                " 8.j",
                " 9.l",
                "10.m",
                "11.n",
                "12.o",
                "13.p",
                "14.r",
                "15.s",
                "16.t",
                "17.u",
                "18.v",
                "19.w",
                "20.x",
                "21.y",
                "22.z");

        final List<String> expected = new ArrayList();
        final Mono<String> missing = Mono.just("s");

        Flux.fromIterable(words)
                .flatMap(word -> Flux.fromArray(word.split("")))
                .concatWith(missing)
                .distinct()
                .sort()
                .zipWith(Flux.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d.%s", count, string))
                .subscribe(expected::add);

        result.stream()
                .forEach(expected::remove);

        Assert.assertTrue(expected.isEmpty());
    }

    //========================================================================================

    @Test
    public void restoringMissingLetterStream() {
        final List<String> result = List.of(
                " 1.a",
                " 2.b",
                " 3.d",
                " 4.e",
                " 5.f",
                " 6.g",
                " 7.h",
                " 8.j",
                " 9.l",
                "10.m",
                "11.n",
                "12.o",
                "13.p",
                "14.r",
                "15.s",
                "16.t",
                "17.u",
                "18.v",
                "19.w",
                "20.x",
                "21.y",
                "22.z");

        final List<String> expected = new ArrayList();
        final Mono<String> missing = Mono.just("s");

        Flux.fromStream(words.stream())
                .flatMap(word -> Flux.fromArray(word.split("")))
                .concatWith(missing)
                .distinct()
                .sort()
                .zipWith(Flux.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d.%s", count, string))
                .subscribe(expected::add);

        result.stream()
                .forEach(expected::remove);

        Assert.assertTrue(expected.isEmpty());
    }

    //========================================================================================

    @Test
    public void restoringMissingLetterToArray() {
        final List<String> result = List.of(
                " 1.a",
                " 2.b",
                " 3.d",
                " 4.e",
                " 5.f",
                " 6.g",
                " 7.h",
                " 8.j",
                " 9.l",
                "10.m",
                "11.n",
                "12.o",
                "13.p",
                "14.r",
                "15.s",
                "16.t",
                "17.u",
                "18.v",
                "19.w",
                "20.x",
                "21.y",
                "22.z");

        final List<String> expected = new ArrayList();
        final Mono<String> missing = Mono.just("s");

        Flux.fromArray(words.toArray())
                .flatMap(word -> Flux.fromArray(word.toString().split("")))
                .concatWith(missing)
                .distinct()
                .sort()
                .zipWith(Flux.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d.%s", count, string))
                .subscribe(expected::add);

        result.stream()
                .forEach(expected::remove);

        Assert.assertTrue(expected.isEmpty());
    }

    //========================================================================================

    @Test
    public void subscribeNonBlocking() {

        Mono.just("Hello")
                .concatWith(Mono.just("wo"))
                .concatWith(Mono.just("rl"))
                .concatWith(Mono.just("d!"))
                .delayElements(Duration.ofMillis(500))
                .subscribe(it -> {
                    Assert.assertTrue(false);
                });
    }

    //========================================================================================

    @Test
    public void subscribeBlockingWithToStream() {
        String expected = "Helloworld!";
        final StringBuilder result = new StringBuilder();

        Mono.just("Hello")
                .concatWith(
                        Mono.just("wo")
                                .delayElement(Duration.ofMillis(100))
                )
                .concatWith(Mono.just("rl"))
                .concatWith(
                        Mono.just("d!")
                                .delayElement(Duration.ofMillis(500))
                )
                .delayElements(Duration.ofMillis(20))
                .toStream()
                .forEach(it -> result.append(it));

        Assert.assertEquals(expected, result.toString());
    }

    //========================================================================================

    @Test
    public void firstWithoutDelaySubcription() {
        String late = "oops I'm late";
        final List<String> first = List.of("let's get", "the party", "started");

        final StringBuilder expected = new StringBuilder()
                .append(first.toString());
        //.append(late);

        final StringBuilder result = new StringBuilder();

        Flux.first(
                Mono.just(late)
                        .delaySubscription(
                                Duration.ofMillis(100)
                        ),
                Flux.just(first)
                        .delayElements(Duration.ofMillis(33))
        )
                .toIterable()
                .forEach(result::append);

        Assert.assertEquals(expected.toString(), result.toString());

    }

    //========================================================================================

    @Test
    public void firstWithDelayElement() {
        String late = "oops I'm late";
        final List<String> first = List.of("let's get", "the party", "started");

        final StringBuilder expected = new StringBuilder()
                .append(late);

        final StringBuilder result = new StringBuilder();

        Flux.first(
                Mono.just(late)
                        .delayElement(
                                Duration.ofMillis(20)
                        ),
                Flux.just(first)
                        .delayElements(
                                Duration.ofMillis(100)
                        )
        )
                .toIterable()
                .forEach(result::append);

        Assert.assertEquals(expected.toString(), result.toString());

    }

    //========================================================================================

    @Test
    public void generate() {
        final StringBuilder result = new StringBuilder();

        final StringBuilder expected = new StringBuilder()
                .append(0)
                .append(3)
                .append(6)
                .append(9)
                .append(12)
                .append(15)
                .append(18)
                .append(21)
                .append(24)
                .append(27)
                .append(30);

        Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next(3 * state);
                    if (state == 10)
                        sink.complete();
                    return state + 1;
                })
                .subscribe(
                        result::append,
                        error -> System.err.println("CAUGHT " + error)
                );

        Assert.assertEquals(expected.toString(), result.toString());
    }

    //========================================================================================


    @Test
    public void take() throws Exception{

        final StringBuilder result = new StringBuilder();

        final StringBuilder expected = new StringBuilder()
                .append(0)
                .append(3)
                .append(6)
                .append(9);

        Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next(3 * state);
                    if (state == 10)
                        sink.complete();
                    return state + 1;
                })
                .toStream()
                .filter( it -> Integer.valueOf(it.toString()) <= 9)
                .forEach(result::append);

        new CountDownLatch(1).await(1000,TimeUnit.MILLISECONDS);

        Assert.assertEquals(expected.toString(), result.toString());
    }

    //========================================================================================


    @Test
    public void generateMutable() {
        final StringBuilder result = new StringBuilder();

        final StringBuilder expected = new StringBuilder()
                .append(0)
                .append(3)
                .append(6)
                .append(9)
                .append(12)
                .append(15)
                .append(18)
                .append(21)
                .append(24)
                .append(27)
                .append(30);

        Flux.generate(
                AtomicInteger::new,
                (state, sink) -> {
                    Integer inc = state.getAndIncrement();
                    sink.next(3 * inc);
                    if (inc == 10)
                        sink.complete();
                    return state;
                })
                .subscribe(
                        result::append,
                        error -> System.err.println("CAUGHT " + error)
                );

        Assert.assertEquals(expected.toString(), result.toString());
    }

    //========================================================================================

    private String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 26) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }

    @Test
    public void handle() {
        final StringBuilder result = new StringBuilder();

        final StringBuilder expected = new StringBuilder()
                .append("O")
                .append("P")
                .append("T");

        Flux.just(-1, 30, 15, 16, 20, 40, 33)
                .handle((i, sink) -> {
                    String letter = alphabet(i);
                    if (i == 40)
                        sink.complete();

                    if (letter != null)
                        sink.next(letter);
                })
                .subscribe(
                        result::append,
                        error -> System.err.println("CAUGHT " + error)
                );

        Assert.assertEquals(expected.toString(), result.toString());
    }

    //========================================================================================

    @Test
    public void using() {
        final StringBuilder result = new StringBuilder();
        String expected = "DISPOSABLE";

        final AtomicBoolean isDisposed = new AtomicBoolean();

        Flux.using(
                () -> new Disposable() {
                    @Override
                    public void dispose() {
                        isDisposed.set(true);
                    }

                    @Override
                    public String toString() {
                        return expected;
                    }
                },
                disposable -> Flux.just(disposable.toString()),
                Disposable::dispose
        ).subscribe(
                result::append,
                System.err::println
        );

        Assert.assertEquals(expected, result.toString());
        Assert.assertTrue(isDisposed.get());

    }

    //========================================================================================

    @Test
    public void range() throws Exception {

        Flux<Integer> oneRange = Flux.range(5, 3);
        Assert.assertEquals(Integer.valueOf(5), oneRange.blockFirst());
        Assert.assertTrue(oneRange.blockFirst() >= Integer.valueOf(5) && oneRange.blockLast() <= 8);

        Flux<Integer> secondRange = Flux.range(6, 2);
        Assert.assertEquals(Integer.valueOf(6), secondRange.blockFirst());
        Assert.assertTrue(secondRange.blockFirst() >= Integer.valueOf(6) && secondRange.blockLast() <= 8);

        Flux<Integer> thirdRante = Flux.range(0, 8);
        Assert.assertEquals(Integer.valueOf(0), thirdRante.blockFirst());
        Assert.assertTrue(thirdRante.blockFirst() >= Integer.valueOf(0) && thirdRante.blockLast() <= 8);
    }

    //========================================================================================

    @Test
    public void doFinally() throws Exception {
        final LongAdder statsCancel = new LongAdder();

        String expected = "foo";
        final StringBuilder result = new StringBuilder();

        Flux.just(expected, "bar")
                .doFinally(type -> {
                    if (type == SignalType.CANCEL)
                        statsCancel.increment();
                })
                .take(1)
                .subscribe(result::append);

        Assert.assertTrue(statsCancel.intValue() == 1);
        Assert.assertEquals(expected, result.toString());

    }

    //========================================================================================

    @Test
    public void onError() throws Exception {
        final LinkedList<String> result = new LinkedList();

        final String expected = "Error";
        Flux.interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3)
                        return "tick " + input;

                    throw new RuntimeException("RuntimeException!!!!");
                })
                .onErrorReturn(expected)
                .subscribe(
                        result::add,
                        System.err::println
                );

        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expected, result.getLast());
    }

    //========================================================================================

    @Test
    public void onErrorRetry() throws Exception {
        final LinkedList<Tuple2<Long, String>> result = new LinkedList();
        final int expected = 6;

        Flux.interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3)
                        return "tick " + input;

                    throw new RuntimeException("RuntimeException!!!!");
                })
                .elapsed()
                .retry(1)
                .subscribe(
                        result::add,
                        System.err::println
                );

        new CountDownLatch(1).await(2500, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expected, result.size());
    }

    //========================================================================================

    @Test
    public void onErrorRetryWhen() throws Exception {

        String expected = "java.lang.RuntimeException: RuntimeException!!!!";

        Flux.<String>error(new IllegalArgumentException())
                .retryWhen(companion ->
                        companion.zipWith(Flux.range(1, 4),
                                (error, index) -> {
                                    if (index < 4)
                                        return index;
                                    else
                                        throw Exceptions.propagate(new Throwable("RuntimeException!!!!"));
                                })
                )
                .doOnError(it -> {
                    Assert.assertEquals(expected, it.toString());
                });

    }

    //========================================================================================

    private String convert(int i) throws IOException {
        if (i > 3)
            throw new IOException("boom " + i);

        return "OK " + i;
    }

    @Test
    public void tryCatch() throws Exception {
        final String expected = "Something bad happened with I/O";

        Flux
                .range(1, 10)
                .map(i -> {
                    try {
                        return convert(i);
                    } catch (IOException e) {
                        throw Exceptions.propagate(
                                new IOException((expected)
                                )
                        );
                    }
                }).subscribe(
                v -> System.out.println("RECEIVED: " + v),
                e -> {
                    if (Exceptions.unwrap(e) instanceof IOException) {
                        Assert.assertEquals(expected, e.getMessage());
                    } else {
                        Assert.assertFalse(true);
                    }
                }
        );
    }

    //========================================================================================

    @Test
    public void log() throws Exception {
        Integer expected = 30;

        Flux.<Integer>range(1, 10)
                .log()
                .take(5)
                .<Integer>handle(
                        (i, sink) -> {
                            if (i == 5)
                                sink.complete();

                            sink.next(Integer.valueOf(i * 2));
                        })
                .collectList()
                .subscribe(
                        it -> Assert.assertTrue(expected == it.stream().mapToInt(Integer::intValue).sum()),
                        error -> System.err.println("CAUGHT " + error)
                );

    }

    //========================================================================================

    @Test
    public void transform() throws Exception {
        final List<String> expected = List.of("BLUE", "GREEN", "PURPLE");

        Flux.fromIterable(List.of("blue", "green", "orange", "purple"))
                .doOnNext(System.out::println)
                .transform(
                        it -> it.filter(color -> !color.equals("orange"))
                                .map(String::toUpperCase)
                )
                .toIterable()
                .forEach(it -> Assert.assertTrue(expected.contains(it)));
    }

    //========================================================================================

    @Test
    public void compose() throws Exception {

        final List<String> expected1 = List.of("BLUE", "GREEN", "PURPLE");
        final List<String> expected2 = List.of("BLUE", "GREEN", "ORANGE");

        final AtomicInteger ai = new AtomicInteger();

        final Flux<String> composedFlux =
                Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
                        .doOnNext(System.out::println)
                        .compose(f -> {
                            if (ai.incrementAndGet() == 1) {
                                return f.filter(color -> !color.equals("orange"))
                                        .map(String::toUpperCase);
                            }

                            return f.filter(color -> !color.equals("purple"))
                                    .map(String::toUpperCase);
                        });

        composedFlux.toIterable()
                .forEach(it -> Assert.assertTrue(expected1.contains(it)));

        composedFlux.toIterable()
                .forEach(it -> Assert.assertTrue(expected2.contains(it)));
    }

    //========================================================================================

    @Test
    public void merge() throws Exception {
        final StringBuilder expected = new StringBuilder().append("blue").append("green").append("orange").append("purple").append("white").append("red");
        final StringBuilder result = new StringBuilder();

        Flux.merge(Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple")))
                .mergeWith(Flux.fromIterable(Arrays.asList("white", "red")))
                .toIterable()
                .forEach(result::append);

        Assert.assertEquals(expected.toString(), result.toString());

    }

    //========================================================================================

    @Test
    public void mergeSequential() throws Exception {

        final StringBuilder expected = new StringBuilder().append("white").append("haze").append("red").append("blue").append("green").append("orange").append("purple");
        final StringBuilder result = new StringBuilder();

        Flux.mergeSequential(
                Flux.fromIterable(
                        Arrays.asList("blue", "green", "orange", "purple")
                )
                        .delayElements(Duration.ofMillis(50))
        )
                .mergeWith(
                        Flux.just("red")
                ).
                delayElements(Duration.ofMillis(40))
                .mergeWith(
                        Flux.fromIterable(
                                Arrays.asList("white", "haze")

                        ))
                .toIterable()
                .forEach(result::append);

        Assert.assertEquals(expected.toString(), result.toString());

    }

    //========================================================================================

    @Test
    public void publishOnIterable() throws Exception {

        final CompletableFuture lock =  new CompletableFuture();
        final StringBuilder expected = new StringBuilder().append("BLUE").append("WHITE").append("RED");
        final StringBuilder result = new StringBuilder();

        Flux.just("blue", "white", "red")
                .publishOn(
                        Schedulers.parallel(),
                        2
                )
                .log()
                .doOnEach(it -> LOG.info("doOnEach: " + it))
                .doOnSubscribe(it -> LOG.info("doOnSubscribe: " + it))
                .doOnComplete(() -> LOG.info("doOnComplete"))
                .doOnTerminate(() -> LOG.info("doOnTerminate"))
                .doAfterTerminate(() -> {
                            LOG.info("doAfterTerminate");
                            lock.complete(null);
                        }
                )
                .doFinally(it ->  LOG.info("doFinally: {}",it))
                .doOnError(it -> LOG.error("doOnError: "+it.getMessage(),it))
                .doOnCancel(() ->  LOG.info("doOnCancel"))
                .map(String::toUpperCase)
                .toIterable()
                .forEach(
                        value ->  {
                            LOG.info("Consumed : {}", value);
                            result.append(value);
                        }
                );

        lock.get();

        Assert.assertEquals(expected.toString(), result.toString());
    }

    //========================================================================================

    @Test
    public void subscribeOnToStream() throws Exception {

        final CompletableFuture lock =  new CompletableFuture();
        final StringBuilder expected = new StringBuilder().append("BLUE").append("WHITE").append("RED");
        final StringBuilder result = new StringBuilder();

        Flux.just("blue", "white", "red")
                .publishOn(
                        Schedulers.parallel(),
                        2
                )
                .subscribeOn(
                        Schedulers.parallel()
                )
                .log()
                .doOnEach(it -> LOG.info("doOnEach: " + it))
                .doOnSubscribe(it -> LOG.info("doOnSubscribe: " + it))
                .doOnComplete(() -> LOG.info("doOnComplete"))
                .doOnTerminate(() -> LOG.info("doOnTerminate"))
                .doAfterTerminate(() -> {
                            LOG.info("doAfterTerminate");
                            lock.complete(null);
                        }
                )
                .doFinally(it ->  LOG.info("doFinally: {}",it))
                .doOnError(it -> LOG.error("doOnError: "+it.getMessage(),it))
                .doOnCancel(() ->  LOG.info("doOnCancel"))
                .map(String::toUpperCase)
                .toStream()
                .forEach(
                value ->  {
                    LOG.info("Consumed : {}", value);
                    result.append(value);
                }
        );

        lock.get();

        Assert.assertEquals(expected.toString(), result.toString());

    }

    //========================================================================================

    @Test
    public void subscribeOnNonBlocking() throws Exception {

        final CompletableFuture lock =  new CompletableFuture();
        final StringBuilder expected = new StringBuilder().append("BLUE 1").append("WHITE 1").append("RED 1").append("BLUE 2").append("WHITE 2").append("RED 2");
        final StringBuilder result = new StringBuilder();

        Flux.just("blue 1", "white 1", "red 1","blue 2", "white 2", "red 2")
                .publishOn(
                        Schedulers.parallel(),
                        3
                )
                .subscribeOn(
                        Schedulers.parallel(),false
                )
                .log()
                .doOnEach(it -> LOG.info("doOnEach: " + it))
                .doOnSubscribe(it -> LOG.info("doOnSubscribe: " + it))
                .doOnComplete(() -> LOG.info("doOnComplete!!!"))
                .doAfterTerminate(() ->  LOG.info("doAfterTerminate"))
                .map(String::toUpperCase)
                .subscribe(
                        value ->  {
                            LOG.info("Consumed : {}", value);
                            result.append(value);
                        },
                        throwable -> LOG.error("Error : {}",throwable),
                        () -> {
                            LOG.info("Completed consumer!!!");
                            lock.complete(null);
                        }
                );

        lock.get();

        Assert.assertEquals(expected.toString(), result.toString());

    }

    //========================================================================================

    @Test
    public void subscribeOnCompletableAsync() throws Exception {

        final List<CompletableFuture> promises =  new LinkedList();
        final List<String>  expected = List.of("BLUE 1","WHITE 1","RED 1","BLUE 2","WHITE 2","RED 2");
        final List<String> result = new ArrayList<>();

        Flux.just("blue 1", "white 1", "red 1","blue 2", "white 2", "red 2")
                .publishOn(
                        Schedulers.parallel(),
                        3
                )
                .subscribeOn(
                        Schedulers.parallel()
                )
                .doOnEach(it -> LOG.info("doOnEach: " + it))
                .map(String::toUpperCase)
                .toStream()
                .forEach(
                        value ->
                                promises.add(
                                    CompletableFuture.runAsync(() ->{
                                        result.add(value);
                                        LOG.info("Consumed : {}", value);
                                    })
                                )
                        );

        CompletableFuture.allOf(promises.toArray(new CompletableFuture[promises.size()]))
                         .get();

        Assert.assertEquals(result.size() , expected.size());


        for (int i = 0; i < expected.size(); i++) {
            Assert.assertTrue(result.contains(expected.get(i)));
        }

    }
}

/**/