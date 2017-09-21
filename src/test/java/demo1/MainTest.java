package demo1;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MainTest {


    @Before
    public void setUp() throws Exception {


    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void just() throws Exception {

        String hello = "Hello";
        String world = "World";

        Set<String> fewWord = Set.of(hello,world);

        Flux<String> fewWords = Flux.just(hello, world);
        fewWords.subscribe(it -> fewWord.remove(it));

        Assert.assertTrue(fewWord.isEmpty());
    }

    @Test
    public void fromIterable() throws Exception {

        final String the = "the";
        final String words1 = "words";
        final String brown = "brown";
        final String fox = "fox";
        final String jumped = "jumped";
        final String over = "over";
        final String lazy = "lazy";
        final String dog = "dog";

        final Set<String> words = Set.of(
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

        Flux<String> manyWords = Flux.fromIterable(words);
        manyWords.subscribe(it -> words.remove(it));

        Assert.assertTrue(words.isEmpty());
    }
}