import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanSearchEngine implements SearchEngine {
    protected Map<String, List<PageEntry>> indexing;

    public BooleanSearchEngine(File pdfsDir) throws IOException {

        List<File> files;

        try (Stream<Path> paths = Files.walk(Paths.get(String.valueOf(pdfsDir)))) {
            files = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }

        indexing = new HashMap<>();

        for (File file : files) {
            var doc = new PdfDocument(new PdfReader(file));

            for (int i = 1; i < doc.getNumberOfPages() + 1; i++) {
                var pdfPage = doc.getPage(i);
                var text = PdfTextExtractor.getTextFromPage(pdfPage);
                var words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>();

                for (var word : words) { // перебираем слова
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                    List<PageEntry> pageEntries;
                    String key = entry.getKey();
                    Integer value = entry.getValue();
                    if (indexing.containsKey(key)) {
                        pageEntries = indexing.get(key);
                    } else {
                        pageEntries = new ArrayList<>();
                    }
                    pageEntries.add(new PageEntry(file.getName(), i, value));
                    indexing.put(key, pageEntries);
                }

                for (var entry : indexing.entrySet()) {
                    Collections.sort(entry.getValue());
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {

        List<PageEntry> list = indexing.get(word.toLowerCase());

        return Objects.requireNonNullElse(list, Collections.emptyList());
    }
}