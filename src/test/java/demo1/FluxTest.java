package demo1;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * @author vicboma
 */
public class FluxTest {

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

    @Test
    public void just() throws Exception {

        String hello = "Hello";
        String world = "World";

        final StringBuilder expected = new StringBuilder(hello)
                .append(world);

        final StringBuilder result = new StringBuilder();

        Flux.just(hello, world)
            .subscribe(result::append);

        Assert.assertEquals(expected.toString(),result.toString());
    }

    @Test
    public void fromIterable() throws Exception {

        final StringBuilder result = new StringBuilder();

        Mono.just("[")
             .concatWith(
                     Flux.fromIterable(words)
                         .filter(word -> !word.equals(dog))
                         .flatMap(word -> Flux.just(word.concat(", ")))
             )
             .concatWith(Flux.just(dog,"]"))
             .subscribe(result::append);


        Assert.assertEquals(words.toString(),result.toString());
    }

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

    @Test
    public void subscribeNonBlocking() {

        Mono.just("Hello")
            .concatWith(Mono.just("wo"))
            .concatWith(Mono.just("rl"))
            .concatWith(Mono.just("d!"))
            .delayElements(Duration.ofMillis(500))
            .subscribe(it ->{
                 Assert.assertTrue(false);
        });
    }

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

        Assert.assertEquals(expected,result.toString());
    }

    @Test
    public void firstEmittingWithoutDelaySubcription() {
        String late = "oops I'm late";
        final List<String> first = List.of("let's get", "the party", "started");

        final StringBuilder expected = new StringBuilder()
                .append(first.toString());
                //.append(late);

        final StringBuilder result = new StringBuilder();

        Flux.firstEmitting(
                Mono.just(late)
                        .delaySubscription(
                                Duration.ofMillis(100)
                        ),
                Flux.just(first)
                    .delayElements(Duration.ofMillis(33))
        )
        .toIterable()
        .forEach(result::append);

        Assert.assertEquals(expected.toString(),result.toString());

    }

    @Test
    public void firstEmittingWithDelayElement() {
        String late = "oops I'm late";
        final List<String> first = List.of("let's get", "the party", "started");

        final StringBuilder expected = new StringBuilder()
                .append(late);

        final StringBuilder result = new StringBuilder();

        Flux.firstEmitting(
                Mono.just(late)
                        .delayElement(
                                Duration.ofMillis(20)
                        ),
                Flux.just(first)
                        .delayElements(
                                Duration.ofMillis(33)
                        )
        )
         .toIterable()
         .forEach(result::append);

        Assert.assertEquals(expected.toString(),result.toString());

    }
}