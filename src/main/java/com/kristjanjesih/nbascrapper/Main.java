package com.kristjanjesih.nbascrapper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Console;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

public class Main {

    private static WebClient webClient;
    private final static String url = "https://www.basketball-reference.com/leagues/NBA_2020_per_game.html";
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws UnsupportedEncodingException {
        webClient = new WebClient();
        setWebClientOptions();
        Scanner console = new Scanner(new InputStreamReader(System.in, "UTF-8"));

        // Type player name
//        Scanner scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.println("Insert player name:");
        String playerName = console.nextLine();
        System.out.println(playerName);

        try {
            // Return player's stats
            System.out.println("Fetching statistics for inserted player...");
            getPage(playerName);
        } catch (Exception ex) {
            LOGGER.error("Scrapper error", ex);
        } finally {
            webClient.close();
        }
    }

    private static void getPage(String playerName) throws IOException {
        HtmlPage page = webClient.getPage(url);
        HtmlPage pageReturn;
        HtmlPage pagePlayerStats;

        // Inputs Form
        HtmlForm form = page.getFormByName("f_big");

        // Submit button
        HtmlSubmitInput button = form.getFirstByXPath("//input[@type='submit']");

        // Text field for player searching
        HtmlTextInput textField = form.getInputByName("search");

        textField.type(playerName);
        pageReturn = button.click();

        HtmlElement title = pageReturn.getFirstByXPath("//h1");
        HtmlElement searchItem;
        HtmlElement playerSearchElement;

        if ("Search Results".equals(title.asText())) {
            searchItem = pageReturn.getFirstByXPath("//div[@class='search-item-url']");
            pagePlayerStats = webClient.getPage("https://www.basketball-reference.com" + searchItem.asText());
            getPlayerStats(pagePlayerStats);
        } else {
            getPlayerStats(pageReturn);
        }

    }

    private static void setWebClientOptions() {
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    private static void getPlayerStats(HtmlPage pageReturn) {
        List<HtmlElement> items = pageReturn.getByXPath("//tr[@class='full_table']");
        if (items.isEmpty()) {
            System.out.println("No statistics found for inserted player!");
            return;
        }

        HtmlElement season;
        HtmlElement threePointAvg;

        for (HtmlElement htmlItem : items) {
            season = (HtmlElement) htmlItem.getFirstByXPath("th[@data-stat='season']");
            threePointAvg = (HtmlElement) htmlItem.getFirstByXPath("td[@data-stat='fg3a_per_g']");
            System.out.println(season.asText() + ": " + threePointAvg.asText());
        }
    }

}
