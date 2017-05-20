/*
 * Copyright (c) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */



import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;

/**
 * A sample application that runs multiple requests against the Drive API. The requests this sample
 * makes are:
 * <ul>
 * <li>Does a resumable media upload</li>
 * <li>Updates the uploaded file by renaming it</li>
 * <li>Does a resumable media download</li>
 * <li>Does a direct media upload</li>
 * <li>Does a direct media download</li>
 * </ul>
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class GoogleDriveAPI {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "Secret_Cloud";

  // private static final String UPLOAD_FILE_PATH = "Enter File Path";
  // private static final String DIR_FOR_DOWNLOADS = "Enter Download Directory";
  // private static final java.io.File UPLOAD_FILE = new java.io.File(UPLOAD_FILE_PATH);

  // /** Directory to store user credentials. */
  // private static final java.io.File DATA_STORE_DIR = new java.io.File(
  // System.getProperty("user.home"), ".store/drive_sample");

  /**
   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
   * globally shared instance across your application.
   */
  private static FileDataStoreFactory dataStoreFactory;

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Global Drive API client. */
  private static Drive drive;

  /** Current Email Credential */
  private static String currentEmail = null;

  /** Authorizes the installed application to access user's protected data. */
  private static Credential authorize() throws Exception {
    // load client secrets
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets
            .load(
                JSON_FACTORY,
                new InputStreamReader(GoogleDriveAPI.class
                    .getResourceAsStream("/client_secrets.json")));
    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out
          .println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
              + "into drive-cmdline-sample/src/main/resources/client_secrets.json");
      System.exit(1);
    }
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
            Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
            .build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  private static void setup(String email) {
    if (currentEmail != email) {
      currentEmail = email;
      try {
        /** Directory to store user credentials. */
        java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/" + email);
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        // authorization
        Credential credential = authorize();
        // set up the global Drive instance
        drive =
            new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                APPLICATION_NAME).build();
      } catch (IOException e) {
        System.err.println(e.getMessage());
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    // Preconditions.checkArgument(
    // !UPLOAD_FILE_PATH.startsWith("Enter ") && !DIR_FOR_DOWNLOADS.startsWith("Enter "),
    // "Please enter the upload file path and download directory in %s", GoogleDriveAPI.class);
    //
    // try {
    // httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    // dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
    // // authorization
    // Credential credential = authorize();
    // // // set up the global Drive instance
    // drive =
    // new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
    // APPLICATION_NAME).build();
    //
    // // run commands
    //
    // View.header1("Starting Resumable Media Upload");
    // File uploadedFile = uploadFile(false);
    //
    // View.header1("Updating Uploaded File Name");
    // File updatedFile = updateFileWithTestSuffix(uploadedFile.getId());
    //
    // View.header1("Starting Resumable Media Download");
    // downloadFile(false, updatedFile);
    //
    // View.header1("Starting Simple Media Upload");
    // uploadedFile = uploadFile(true);
    //
    // View.header1("Starting Simple Media Download");
    // downloadFile(true, uploadedFile);
    //
    // View.header1("Success!");
    // return;
    // } catch (IOException e) {
    // System.err.println(e.getMessage());
    // } catch (Throwable t) {
    // t.printStackTrace();
    // }
    // System.exit(1);
    // setup();
    // upload("D:\\桌面\\ISSUE.txt");
  }

  /** Uploads a file using either resumable or direct media upload. */
  public static String upload(String email, String filepath) throws IOException {
    setup(email);
    FileContent mediaContent =
        new FileContent("application/octet-stream", new java.io.File(filepath));
    Drive.Files.Insert insert = drive.files().insert(new File().setTitle(filepath), mediaContent);
    MediaHttpUploader uploader = insert.getMediaHttpUploader();
    // uploader.setDirectUploadEnabled(useDirectUpload);
    uploader.setProgressListener(new FileUploadProgressListener());
    return insert.execute().getId();
  }

  /** Downloads a file using either resumable or direct media download. */
  public static void download(String email, String fileID, String downFolder) throws IOException {
    setup(email);
    // create parent directory (if necessary)
    java.io.File parentDir = new java.io.File(downFolder);
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("Unable to create parent directory");
    }

    // OutputStream out = new FileOutputStream(new java.io.File(parentDir,
    // uploadedFile.getTitle()));
    OutputStream out = new FileOutputStream(new java.io.File(parentDir, fileID));

    File fileToDownload = drive.files().get(fileID).execute();
    MediaHttpDownloader downloader =
        new MediaHttpDownloader(httpTransport, drive.getRequestFactory().getInitializer());
    // downloader.setDirectDownloadEnabled(useDirectDownload);
    downloader.setProgressListener(new FileDownloadProgressListener());
    downloader.download(new GenericUrl(fileToDownload.getDownloadUrl()), out);
  }

  /** Delete a file on Google Drive. */
  /**
   * Permanently delete a file, skipping the trash.
   *
   * @param fileID ID of the file to delete.
   */
  public static void delete(String email, String fileID) throws IOException {
    setup(email);
    drive.files().delete(fileID).execute();
  }

}
