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

        Set<String> set = Set.of(hello,world);

        Flux<String> fewWords = Flux.just(hello, world);
        fewWords.subscribe(it -> set.remove(it));

        Assert.assertTrue(set.isEmpty());
    }


}