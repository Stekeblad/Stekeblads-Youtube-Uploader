package io.github.stekeblad.youtubeuploader.youtube.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoCategory;
import com.google.api.services.youtube.model.VideoCategoryListResponse;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import io.github.stekeblad.youtubeuploader.youtube.Auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum CategoryUtils {
    INSTANCE;

    private ConfigManager configManager = ConfigManager.INSTANCE;

    private HashMap<String, String> categories = null;

    public void downloadCategories() {
        String lang = configManager.getCategoryLanguage();
        String region = configManager.getCategoryCountry();

        if(lang.length() != 2 || region.length() != 2) {
            System.err.println("Invalid length of lang or region \nlang: " + lang + "\nregion: " + region);
            return;
        }

        try {
            Credential creds = Auth.authUser();
            YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                    "Stekeblads Youtube Uploader").build();

            YouTube.VideoCategories.List videoCategoriesListForRegionRequest = youtube.videoCategories().list("snippet");
            videoCategoriesListForRegionRequest.setHl(lang);
            videoCategoriesListForRegionRequest.setRegionCode(region);
            VideoCategoryListResponse response = videoCategoriesListForRegionRequest.execute();

            List<VideoCategory> vidCat = response.getItems();
            categories = new HashMap<>();
            categories.put("Select a category", "-1");
            for(VideoCategory cat : vidCat) {
                categories.put(cat.getSnippet().getTitle(), cat.getId());
            }
            saveCategories();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    public ArrayList<String> getCategoryNames() {
        if (categories == null) {
            loadCategories();
        }
        return new ArrayList<>(categories.keySet());
    }

    public String getCategoryId(String categoryName) {
        return categories.get(categoryName);
    }

    private void saveCategories() {
        StringBuilder saveString = new StringBuilder();
        categories.forEach((k, v) -> saveString.append(v).append(":").append(k).append("\n"));
        saveString.deleteCharAt(saveString.length() - 1);
        configManager.saveLocalizedCategories(saveString.toString());
    }

    public void loadCategories() {
        categories = new HashMap<>();
        ArrayList<String> data = configManager.loadLocalizedCategories();
        for (String category : data) {
            String id = category.substring(0, category.indexOf(':'));
            String name = category.substring(category.indexOf(':') + 1);
            categories.put(name, id);
        }
    }
}
