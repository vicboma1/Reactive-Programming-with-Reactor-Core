package demo1;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import reactor.core.publisher.Flux;

import java.util.*;

public class FluxTest {

    final String the = "the";
    final String words1 = "words";
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
        final Set<String> fewWord = Sets.newSet(hello, world);

        final Flux<String> fewWords = Flux.just(hello, world);
        fewWords.subscribe(fewWord::remove);

        Assert.assertTrue(fewWord.isEmpty());
    }

    @Test
    public void fromIterable() throws Exception {

        final Set<String> result = new HashSet<>();

        Flux.fromIterable(words)
                .subscribe(result::add);

        final boolean removeAll = result.removeAll(words);
        Assert.assertTrue(removeAll);
    }

    @Test
    public void findingMissingLetter() {
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
}