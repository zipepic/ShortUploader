package com.example.shortuploader.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class YoutubeUploader {
    @Value("google.oauth")
    private String OAUTH_TOKEN;
    public String upload() throws IOException {

        YouTube youtube = new YouTube.Builder( new NetHttpTransport(),  new JacksonFactory(), authorize())
                .setApplicationName("youtube-cmdline-search-sample").build();

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("public");

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle("Цитата про бобров");
        snippet.setDescription("Посмотри до конца!");

        VideoContentDetails contentDetails = new VideoContentDetails();
        contentDetails.setContentRating(new ContentRating().setYtRating("ytAgeRestricted"));

        Video videoObjectDefiningMetadata = new Video();
        videoObjectDefiningMetadata.setSnippet(snippet);
        videoObjectDefiningMetadata.setStatus(status);

        File mediaFile = new File("/Users/xzz1p/Desktop/test2.mp4");
        InputStreamContent mediaContent = new InputStreamContent("video/*",
                new FileInputStream(mediaFile));
        mediaContent.setLength(mediaFile.length());

        YouTube.Videos.Insert videoInsert = youtube.videos()
                .insert("snippet,status", videoObjectDefiningMetadata, mediaContent);

        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(false);
        uploader.setProgressListener(new MediaHttpUploaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
                switch (mediaHttpUploader.getUploadState()) {
                    case INITIATION_STARTED:
                        System.out.println("Initiation Started");
                        break;
                    case INITIATION_COMPLETE:
                        System.out.println("Initiation Completed");
                        break;
                    case MEDIA_IN_PROGRESS:
                        System.out.println("Upload in progress");
                        System.out.println("Upload percentage: " + uploader.getProgress());
                        break;
                    case MEDIA_COMPLETE:
                        System.out.println("Upload Completed!");
                        break;
                }
            }
        });

        Video returnedVideo = videoInsert.execute();

        return returnedVideo.getId();
    }
    private GoogleCredential authorize() {
        return new GoogleCredential().setAccessToken(OAUTH_TOKEN);
    }
}
