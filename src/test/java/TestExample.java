import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.junit.Before;
import org.junit.After;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class TestExample {
    private static final Logger logger
            = Logger.getLogger(TestExample.class.getName());
    WebDriver driver;

    @Before
    public void startDriver() {
        driver = new FirefoxDriver();
    }

    @After
    public void stopDriver() {
        driver.close();
    }

    @Test
    public void test() throws Exception {

        driver.get("https://market.yandex.ru/");

        driver.findElement(By.cssSelector(
                "#index-headline-id-tab-1 > span.main-tabs__text")).click();

        driver.findElement(By.xpath(
                "(//a[contains(text(),'Мобильные телефоны')])[2]")).click();

        driver.findElement(By.xpath(
                "//a[contains(text(), 'расширенный поиск')]")).click();


        driver.findElement(By.cssSelector("#gf-pricefrom-var"))
              .sendKeys("5125");
        driver.findElement(By.cssSelector("#gf-priceto-var"))
              .sendKeys("10123");
        driver.findElement(By.cssSelector("#glf-in-stock-select"))
              .click();
        driver.findElement(By.xpath("//*[contains(@class, 'title')]/"
                + "span[contains(text(), 'Платформа')]")).click();
        driver.findElement(By.xpath(
                "//div[contains(@class, 'filter-panel-aside')]//"
                        + "*[contains(text(), 'Android')]/..//input")).click();

        // Wait until page is updated
        Thread.sleep(15_000);

        List<WebElement> ratings = driver.findElements(
                By.xpath("//*[contains(@class, 'snippet-card')]"
                        + "//div[contains(@class, 'rating') and @date-rate]"));

        List<Integer> indexes = getIndexesWithRating(ratings);
        Assert.assertTrue("There should be at least three phones with rating "
                + "between 3.5 and 4.5", indexes.size() >= 3);

        int[] selectedIndexes=selectRandomIndexes(indexes);
        for (int i = 0; i < selectedIndexes.length; i++) {
            int phoneIndex = selectedIndexes[i];
            WebElement phone = driver.findElement(By.xpath(
                    getPhoneXPathExpr(phoneIndex)));

            String title = phone.findElement(By.cssSelector(
                    "span.snippet-card__header-text")).getText();

            String priceFrom = driver.findElement(By.xpath(
                    getPhonesPriceXPathExpr(phoneIndex, 0))).getText();
            String priceTo = driver.findElement(By.xpath(
                    getPhonesPriceXPathExpr(phoneIndex, 1))).getText();

            logger.info(String.format("[%d] %s стоимость %s %s",
                    phoneIndex, title, replaceThinSP(priceFrom),
                    replaceThinSP(priceTo)));
        }
    }

    private static List<Integer> getIndexesWithRating(
            List<WebElement> ratings) {
        List<Integer> indexes = new LinkedList<>();

        int index = 0;
        for (WebElement rating : ratings) {
            float ratingValue = Float.valueOf(rating.getText());
            if (ratingValue >= 3.5 && ratingValue <= 4.5) {
                indexes.add(index);
            }
            index++;
        }
        return indexes;
    }

    private static int[] selectRandomIndexes(List<Integer> indexes) {
        Random random = new Random();
        int[] selectedIndexes = new int[3];

        for (int i = 0; i < selectedIndexes.length; i++) {
            selectedIndexes[i] = indexes.remove(random.nextInt(indexes.size()));
        }
        return selectedIndexes;
    }

    private static String getPhoneXPathExpr(int zeroBasedIndex) {
        return String.format("(//div[contains(@class, 'snippet-card') "
                + "and ./div[@class = 'snippet-card__view']])[%d]",
                zeroBasedIndex + 1);
    }

    private static String getPhonesPriceXPathExpr(int zeroBasedPhoneIndex,
                                                  int zeroBasedPriceIndex) {
        return String.format("((%s)//*[@class = 'price'])[%d]",
                getPhoneXPathExpr(zeroBasedPhoneIndex),
                zeroBasedPriceIndex + 1);
    }

    private static String replaceThinSP(String str) {
        return str.replaceAll("\u2009", "");
    }
}
