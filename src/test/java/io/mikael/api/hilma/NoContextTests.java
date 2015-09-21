package io.mikael.api.hilma;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NoContextTests {


    @Test
    public void testDeeTees() throws Exception {
        final File f = Paths.get("src/test/resources/www/posts/2014-011982.html").toFile();
        try (final InputStream fis = new FileInputStream(f)) {
            final Document doc = Jsoup.parse(fis, "UTF-8", "/fi/notice/view/2014-011132/");
            final Elements deetees = doc.select("div#mainContent > dl > dt");
            deetees.stream().forEach(first -> {
                final Element second = first.nextElementSibling();
                System.err.println(first.text());
                System.err.println(second.text());
                second.select("table.CONTACT[summary]").stream()
                        .map(NoContextTests::contactSummaryTableToMap)
                        .forEach(System.err::println);
            });
        }
    }


    private static Map<String, String> contactSummaryTableToMap(final Element table) {
        return table.select("tr").stream()
                .map(tr -> tr.select("td"))
                .filter(tds -> tds.size() == 2)
                .collect(Collectors.toMap(
                        tds -> tds.get(0).text(), tds -> tds.get(1).text(),
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

}
