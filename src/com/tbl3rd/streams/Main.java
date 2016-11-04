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

    private static final Pattern splitter = Pattern.compile("\\W+");

    // Hide checked exceptions from lambda expressions.
    //
    private static Stream<String> filesLinesUnchecked(Path path) {
        try {
            return Files.lines(path);
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    // Return {word {fileName [index ...]}} for words in fileName.
    //
    private static Map<String, Map<String, List<Integer>>>
        indexWordsInFile(String fileName) {
        final Path path = Paths.get(fileName);
        final List<String> words = filesLinesUnchecked(path)
            .flatMap(splitter::splitAsStream)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        final Map<String, List<Integer>> indexes =
            IntStream.range(0, words.size()).boxed().collect(
                Collectors.groupingBy(words::get));
        final Map<String, Map<String, List<Integer>>> result =
            indexes.entrySet().stream().collect(
                Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.toMap(v -> fileName,
                                     Map.Entry::getValue)));
        result.forEach((name, index) -> System.out.println(
                           format("{0}: {1}", name, index)));
        return result;
    }

    private static Map<String, List<Map<String, List<Integer>>>> collate(
        Function<String, Map<String, Map<String, List<Integer>>>> mapWords,
        String[] fileNames) {
        return Arrays.stream(fileNames).map(mapWords)
            .map(Map::entrySet).flatMap(Collection::stream)
            .collect(Collectors.groupingBy(
                         Map.Entry::getKey,
                         Collectors.mapping(Map.Entry::getValue,
                                            Collectors.toList())));
    }

    public static void main(String[] argv) {
        final Map<String, List<Map<String, List<Integer>>>> index =
            collate(Main::indexWordsInFile, argv);
        index.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .forEach(e -> System.out.println(
                         format("{0} : {1}", e.getKey(), e.getValue())));
    }
}
