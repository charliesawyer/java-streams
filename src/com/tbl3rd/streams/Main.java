package com.tbl3rd.streams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

public class Main {

    // a regular expression to split strings into words
    //
    private static final Pattern splitter = Pattern.compile("\\W+");

    // Return File.lines(PATH) with unchecked exceptions.
    //
    private static Stream<String> filesLinesUnchecked(Path path) {
        try {
            return Files.lines(path);
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    // Return the words from FILENAME.
    //
    private static List<String> wordsFromFile(String fileName) {
        return filesLinesUnchecked(Paths.get(fileName))
            .flatMap(splitter::splitAsStream)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    }

    // Return {word {FILENAME count}} after counting each word in
    // fileName.
    //
    private static Map<String, Map<String, Long>>
    countWords(String fileName) {
        final List<String> words = wordsFromFile(fileName);
        final Map<String, Long> counts =
            words.stream().collect(
                Collectors.groupingBy(Function.identity(),
                                      Collectors.counting()));
        return counts.entrySet().stream().collect(
            Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.toMap(e -> fileName,
                                 Map.Entry::getValue)));
    }

    // Return {word {FILENAME [index ...]}} for words in fileName.
    //
    private static Map<String, Map<String, List<Integer>>>
        indexWords(String fileName) {
        final List<String> words = wordsFromFile(fileName);
        final Map<String, List<Integer>> indexes =
            IntStream.range(0, words.size()).boxed().collect(
                Collectors.groupingBy(words::get));
        return indexes.entrySet().stream().collect(
            Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.toMap(v -> fileName,
                                 Map.Entry::getValue)));
    }

    // Apply MAPWORDS to FILENAMES and collate on words.
    //
    private static <V> Map<String, List<Map<String, V>>> collate(
        Function<String, Map<String, Map<String, V>>> mapWords,
        String[] fileNames) {
        return Arrays.stream(fileNames).map(mapWords)
            .map(Map::entrySet).flatMap(Collection::stream)
            .collect(Collectors.groupingBy(
                         Map.Entry::getKey,
                         Collectors.mapping(Map.Entry::getValue,
                                            Collectors.toList())));
    }

    // Show the COLLATION on System.out.
    //
    private static <V> void show(Map<String, V> collation) {
        collation.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .forEach(e -> System.out.println(
                         format("  {0} : {1}", e.getKey(), e.getValue())));
    }

    public static void main(String[] argv) {
        System.out.println();
        System.out.println("Count words in files.");
        show(collate(Main::countWords, argv));
        System.out.println();
        System.out.println("Index words in files.");
        show(collate(Main::indexWords, argv));
        System.out.println();
    }
}
