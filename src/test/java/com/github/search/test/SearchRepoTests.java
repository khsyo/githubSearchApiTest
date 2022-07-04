package com.github.search.test;

import com.github.search.api.BaseRequests;
import io.qameta.allure.Description;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.AssertJUnit.assertTrue;

public class SearchRepoTests extends BaseTest {

    @Description("Filter search results by Java")
    @Test(description = "Should get repositories of Java language")
    public void getGithubReposForAGivenLanguage() throws UnsupportedEncodingException {
        String queryKeyword = URLEncoder.encode("pacman+language:java", StandardCharsets.UTF_8.name());

        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("q", queryKeyword);
        queryParams.put("sort", "stars");
        queryParams.put("order", "desc");
        queryParams.put("per_page", 20);

        Response response = BaseRequests.get(queryParams);

        JsonPath jsonPath = response.jsonPath();

        List<HashMap<String, Object>> items = jsonPath.getList("items");
        HashSet<String> languageSet = new HashSet<String>();

        for(HashMap<String, Object> item : items){
            languageSet.add((String)item.get("language"));
        }

        // Assert that search only results of Java language
        assertThat(languageSet.size(), is(1));
        assertThat(languageSet.contains("Java"), is(true));
        assertThat(languageSet.contains("Javascript"), is(false));
    }

    @Test(description = "Should get repositories created after 2020-01-01")
    public void filterSearchResultsByCreationDate() throws UnsupportedEncodingException, ParseException {
        HashMap<String, Object> queryParams = new HashMap<>();
        String queryKeyword = URLEncoder.encode("vangogh language:javascript created:>2020-01-01 sort:updated-desc", StandardCharsets.UTF_8.name());
        queryParams.put("q", queryKeyword);
        queryParams.put("sort", "updated");
        queryParams.put("order", "desc");
        queryParams.put("per_page", 20);

        Response response = BaseRequests.get(queryParams);
        JsonPath jsonPath = response.jsonPath();

        List<HashMap<String, Object>> items = jsonPath.getList("items");
        for(int i = 0; i < items.size()-1; i++){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date dateTime1 = sdf.parse((String)items.get(i).get("updated_at"));
            Date dateTime2 = sdf.parse((String)items.get(i+1).get("updated_at"));

            // Assert the order of creation date is correct, assert order is descending as specified
            assertTrue(dateTime1.after(dateTime2));
        }
    }

    @Test(description = "Should get results of either Javascript or Typescript languages")
    public void searchRepoByMoreThanOneProgrammingLanguage() throws UnsupportedEncodingException {
        String queryKeyword = URLEncoder.encode("pacman language:Javascript language:Typescript", StandardCharsets.UTF_8.name());

        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("q", queryKeyword);

        Response response = BaseRequests.get(queryParams);
        JsonPath jsonPath = response.jsonPath();

        List<HashMap<String, Object>> items = jsonPath.get("items");
        HashSet<String> languageSet = new HashSet<>();
        for(HashMap<String, Object> item : items){
            languageSet.add((String)item.get("language"));
        }

        //Assert that language is either javascript/typescript
        assertThat(languageSet.size(), is(2));
        assertThat(languageSet.contains("TypeScript"), is(true));
        assertThat(languageSet.contains("JavaScript"), is(true));
    }

    @Test(description = "Validate item against schema")
    public void getReposByUserAndValidateSchema() throws URISyntaxException, UnsupportedEncodingException {
        String queryKeyword = URLEncoder.encode("user:khsyo", StandardCharsets.UTF_8.name());
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("q", queryKeyword);
        queryParams.put("per_page", 1);
        String file = "search_repositories_schema.json";

        //Validate schema
        BaseRequests.getAndValidateSchema(queryParams, file);
    }

    @Test(description = "Validate results only contain repositories of single user")
    public void getReposByUserAndValidateOneUserReposOnly() throws UnsupportedEncodingException {
        String queryKeyword = URLEncoder.encode("user:khsyo", StandardCharsets.UTF_8.name());

        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("q", queryKeyword);

        String responseStr = BaseRequests.get(queryParams).asString();

        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray jsonArray = jsonObject.getJSONArray("items");

        HashSet<String> userSet = new HashSet<>();
        for(int i = 0; i < jsonArray.length(); i++){
            String login = jsonArray.getJSONObject(i).
                    getJSONObject("owner").
                    getString("login");
            userSet.add(login);
        }

        //Assert that only one user's repo is returned
        assertThat(userSet.size(), is(1));
        assertThat(userSet.contains("khsyo"), is(true));
    }

    @Test(description = "Query that exceed 256 characters should get error")
    public void queryExceed256CharsShouldGetError() throws UnsupportedEncodingException {
        String queryKeyword = URLEncoder.encode("marioadfnoafaoadfafpjaposfjapsofjadpofapsofjapdnfapsodnapoifnpaufnpaiubfaiuebfiqcwnopqnwvwaiygrbifgasdofiajoiajoiajoiajsfoiajsfodahjspofhaspofhaspoiahpofiasdfasadfsnaofaofadoafdsooadouafoadfoafouahfouahfuasdhfuashfuaohfdaushfasuhauofhauosfhaousfhauosfhasouhasdfouashfuoashdoudfashouadsfhdaosuhdasohadsfufadhsuadshfoasdhfoaudsfa", StandardCharsets.UTF_8.name());
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("q", queryKeyword);

        Response response = BaseRequests.getAndExpectError(queryParams, 422);

        // Assert getting expected error message
        assertThat(response.jsonPath().<String>get("message"), equalTo("Validation Failed"));
    }
}
