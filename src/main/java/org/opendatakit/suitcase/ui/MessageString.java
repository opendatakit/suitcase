package org.opendatakit.suitcase.ui;

import java.util.List;

public class MessageString {
  public static final String NEW_LINE = System.lineSeparator();

  // ODK names
  private static final String CLOUD_ENDPOINT = "Cloud Endpoint";
  private static final String SUITCASE = "org.opendatakit.suitcase.Suitcase";
  private static final String TABLE_ID = "Table ID";
  private static final String APP_ID = "App ID";

  // Error message
  public static final String GENERIC_ERR = "Error occurred";
  public static final String BAD_URL = CLOUD_ENDPOINT + " address is invalid.";
  public static final String BAD_CRED = CLOUD_ENDPOINT + " address, " + APP_ID + ", username, or password is invalid. Please check your credentials.";
  public static final String DATA_DIR_INVALID = "Data directory does not contain \"assets\" directory or \"tables\" directory.";
  public static final String INVALID_CSV = "Data directory contains invalid CSV.";
  public static final String VISIT_WEB_ERROR = "Please visit" + CLOUD_ENDPOINT + "web interface for error detail.";

  // unable to ... error
  private static final String UNABLE_PREFIX = "Unable to ";
  public static final String IO_DELETE_ERR = UNABLE_PREFIX + "delete data.";
  public static final String IO_WRITE_ERR = UNABLE_PREFIX + "write file.";
  public static final String IO_READ_ERR = UNABLE_PREFIX + "read file.";
  public static final String HTTP_IO_ERROR = UNABLE_PREFIX + "reach " + CLOUD_ENDPOINT + " server.";

  // ... does not exist error
  private static final String NONEXISTENCE_SUFFIX = " does not exist";
  public static final String DATA_DIR_NOT_EXIST = "Data directory" + NONEXISTENCE_SUFFIX;

  // ... cannot be empty error
  private static final String CANNOT_EMPTY_SUFFIX = " cannot be empty.";
  public static final String CLOUD_ENDPOINT_EMPTY = CLOUD_ENDPOINT + " address" + CANNOT_EMPTY_SUFFIX;
  public static final String APP_ID_EMPTY = APP_ID + CANNOT_EMPTY_SUFFIX;
  public static final String TABLE_ID_EMPTY = TABLE_ID + CANNOT_EMPTY_SUFFIX;
  public static final String USERNAME_EMPTY = "Username" + CANNOT_EMPTY_SUFFIX;
  public static final String PASSWORD_EMPTY = "Password" + CANNOT_EMPTY_SUFFIX;
  public static final String VERSION_EMPTY = "Version" + CANNOT_EMPTY_SUFFIX;
  public static final String DATA_PATH_EMPTY = "Data path" + CANNOT_EMPTY_SUFFIX;
  public static final String SAVE_PATH_EMPTY = "Save path" + CANNOT_EMPTY_SUFFIX;

  // Prompts
  public static final String OVERWRITE_DATA = "Data from a previous session detected. Delete existing data and download data from the Cloud Endpoint?";

  public static String getOverwriteCsvString(List<String> tableIds) {
    String messagePrefix = "This CSV for table Id ";
    String messageSuffix = " has already been downloaded. Delete existing CSV and download data from the Cloud Endpoint?";
    StringBuilder message = new StringBuilder(messagePrefix);
    for(int i=0;i<tableIds.size();i++){
      message.append(tableIds.get(i));
      if(i<tableIds.size()-2){
        message.append(", ");
      }
      else if(i==tableIds.size()-2){
        message.append(" and ");
      }
    }
    message.append(messageSuffix);
    return message.toString();
  }

  public static String getBadTableIdString(String tableId){
    return TABLE_ID + " " + tableId + NONEXISTENCE_SUFFIX;
  }
}
