package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.model.CsvConfig;
import org.opendatakit.suitcase.model.ODKCsv;
import org.apache.commons.cli.*;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.net.*;
import org.opendatakit.suitcase.utils.FieldsValidatorUtils;
import org.opendatakit.suitcase.utils.FileUtils;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import static org.opendatakit.suitcase.ui.MessageString.*;

public class SuitcaseCLI {

  public static final int PARAM_ERROR_CODE = 1;
  private enum Operation {
    DOWNLOAD, UPLOAD, UPDATE, RESET, INFO, TABLE_OP, PERMISSION
  }

  private static final String DOWNLOAD_OP = "download";
  private static final String UPLOAD_OP = "upload";
  private static final String RESET_OP = "reset";
  private static final String UPDATE_OP = "update";
  private static final String TABLE_OP = "tableOp";
  private static final String PERMISSION_OP = "permission";

  private static final String HELP_OPT = "help";
  private static final String HELP_OPT_SHORT = "h";

  private static final String VERSION_OPT_SHORT = "v";
  private static final String VERSION_OPT = "version";

  private static final String CLOUD_ENDPOINT_URL_OPT = "cloudEndpointUrl";
  private static final String APP_ID_OPT = "appId";
  private static final String TABLE_ID_OPT = "tableId";
  private static final String USERNAME_OPT = "username";
  private static final String PASSWORD_OPT = "password";

  private static final String DATA_VERSION_OPT = "dataVersion";

  private static final String ATTACHMENT_OPT = "attachment";
  private static final String ATTACHMENT_OPT_SHORT = "a";

  private static final String SCAN_OPT = "scan";
  private static final String SCAN_OPT_SHORT = "s";

  private static final String EXTRA_OPT = "extra";
  private static final String EXTRA_OPT_SHORT = "e";

  private static final String PATH_OPT = "path";
  private static final String UPLOAD_OP_OPT = "uploadOp";
  private static final String RELATIVE_SERVER_PATH_OPT = "relativeServerPath";

  private static final String FORCE_OPT = "force";
  private static final String FORCE_OPT_SHORT = "f";

  private static final String UPDATE_LOG_PATH_OPT = "updateLogPath";
  private static final String DEFAULT_DATA_VERSION = "2";



  private static final String[] REQUIRED_ARGS = new String[]{"cloudEndpointUrl", "appId"};

  private String[] args;

  private Options cliOptions;
  private CloudEndpointInfo cloudEndpointInfo;

  private String tableId;
  private String version;
  private String path;
  private String relativeServerPath;
  private String updateLogPath;
  private String tableOp;
  private String uploadOp;
  private boolean downloadAttachment;
  private boolean scanFormatting;
  private boolean extraMetadata;
  private boolean force;
  

  public SuitcaseCLI(String[] args) {
    this.args = args;

    this.cliOptions = buildOptions();
  }

  public int startCLI() {
    int retCode = 0;
    Operation operation = parseArgs(args, cliOptions);

    if (operation == null) {
      // this means some error was found when parsing arguments
      retCode = PARAM_ERROR_CODE;
      return retCode;
    }

    String error;

    switch (operation) {
    case DOWNLOAD:
      CsvConfig config = new CsvConfig(downloadAttachment, scanFormatting, extraMetadata);

      List<String> tableIds = Collections.singletonList(tableId);
      error = FieldsValidatorUtils.checkDownloadFields(tableIds, path, cloudEndpointInfo);
      if (error != null) {
        DialogUtils.showError(error, false);
        retCode = PARAM_ERROR_CODE;
      } else {
        retCode = new DownloadTask(cloudEndpointInfo, tableIds, config, path, false).blockingExecute();
      }
      break;
    case UPLOAD:
      error = FieldsValidatorUtils.checkUploadFields(version, path, uploadOp);

      if (error != null) {
        DialogUtils.showError(error, false);
        retCode = PARAM_ERROR_CODE;
      } else {
        retCode = new UploadTask(cloudEndpointInfo, path, version, false, uploadOp, relativeServerPath).blockingExecute();
      }
      break;
    case RESET:
      error = FieldsValidatorUtils.checkResetFields(version);

      if (error != null) {
        DialogUtils.showError(error, false);
        retCode = PARAM_ERROR_CODE;
      } else {
        retCode = new ResetTask(version, false).blockingExecute();
      }
      break;

    case UPDATE:
      error = FieldsValidatorUtils.checkUpdateFields(tableId, path, version);

      if (error != null) {
        DialogUtils.showError(error, false);
        retCode = PARAM_ERROR_CODE;
      } else {
        retCode = new UpdateTask(cloudEndpointInfo, path, version, tableId, updateLogPath , false).blockingExecute();
      }
      break;

    case TABLE_OP:
      error = FieldsValidatorUtils.checkTableOpFields(tableId, path, version, tableOp);

      if (error != null) {
        DialogUtils.showError(error, false);
        retCode = PARAM_ERROR_CODE;
      } else {
        retCode = new TableTask(cloudEndpointInfo, tableId, path, version, tableOp, false).blockingExecute();
      }
      break;

    case PERMISSION:
      error = FieldsValidatorUtils.checkPermissionFields(version, path);

      if (error != null) {
        DialogUtils.showError(error, false);
        retCode = PARAM_ERROR_CODE;
      } else {
        retCode = new PermissionTask(cloudEndpointInfo, path, version, false).blockingExecute();
      }

      break;
    }

    return retCode;
  }

  private Options buildOptions() {
    Options opt = new Options();

    //operations
    OptionGroup operation = new OptionGroup();
    operation.addOption(new Option(DOWNLOAD_OP, false, "Download csv"));
    operation.addOption(new Option(UPLOAD_OP, false, "Upload one file or all files in directory"));
    operation.addOption(new Option(RESET_OP, false, "Reset server"));
    operation.addOption(new Option(UPDATE_OP, false, "Update tableId using csv specified by path"));
    operation.addOption(new Option(TABLE_OP, true, "Create, delete, or clear tableId using csv specified by path"));
    operation.addOption(new Option(PERMISSION_OP, false, "Upload user permissions using csv specified by path"));
    operation.addOption(new Option(HELP_OPT_SHORT, HELP_OPT, false, "print this message"));
    operation.addOption(new Option(VERSION_OPT_SHORT, VERSION_OPT, false, "prints version information"));
    operation.setRequired(true);
    opt.addOptionGroup(operation);

    //Cloud Endpoint related
    Option cloudEndpointUrl = new Option(CLOUD_ENDPOINT_URL_OPT, true, "url to Cloud Endpoint server");
    opt.addOption(cloudEndpointUrl);

    Option appId = new Option(APP_ID_OPT, true, "app id");
    opt.addOption(appId);

    Option tableId = new Option(TABLE_ID_OPT, true, "table id");
    opt.addOption(tableId);

    opt.addOption(USERNAME_OPT, true, "username"); // not required
    opt.addOption(PASSWORD_OPT, true, "password"); // not required

    // not required for download, check later
    opt.addOption(DATA_VERSION_OPT, true, "version of data, usually 1 or 2");


    //csv options
    opt.addOption(ATTACHMENT_OPT_SHORT, ATTACHMENT_OPT, false, "download attachments");
    opt.addOption(SCAN_OPT_SHORT, SCAN_OPT, false, "apply Scan formatting");
    opt.addOption(EXTRA_OPT_SHORT, EXTRA_OPT, false, "add extra metadata columns");

    opt.addOption(PATH_OPT, true, "Specify a custom path to output csv or to upload from. "
                              + "Default csv directory is ./Download/ "
                              + "Default upload directory is ./Upload/ ");

    opt.addOption(UPLOAD_OP_OPT, true, "Specify the uploadop to either FILE or RESET_APP."
                                   + "This option must be used with upload option."
                                   + "RESET_APP is the default option and will push all files to server"
                                   + "FILE is used to push one file to relativeServerPath");

    opt.addOption(RELATIVE_SERVER_PATH_OPT, true, "Specify the relative server path to push file to");

    //UI
    opt.addOption(FORCE_OPT_SHORT, FORCE_OPT, false, "do not prompt, overwrite existing files");

    //Update Log
    opt.addOption(UPDATE_LOG_PATH_OPT, true, "Specify a custom path to create update log file. "
        + "Default directory is ./Update");

    return opt;
  }

  /**
   * Parses user arguments from pre-specified Options.
   *
   * @param args    Arguments passed to by user
   * @param options Options to parse from
   * @return false when either "-h" or "-v" is passed, otherwise true
   */
  private Operation parseArgs(String[] args, Options options) {
    Operation operation = null;

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine line = parser.parse(options, args);

      //handle -h and --help
      if (line.hasOption(HELP_OPT_SHORT)) {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("suitcase", options);
        return Operation.INFO;
      }

      //handle -v
      if (line.hasOption(VERSION_OPT_SHORT)) {
        System.out.println("ODK org.opendatakit.suitcase.Suitcase 2.0");
        return Operation.INFO;
      }

      if (line.hasOption(UPLOAD_OP)) {
        operation = Operation.UPLOAD;
      } else if (line.hasOption(RESET_OP)) {
        operation = Operation.RESET;
      } else if (line.hasOption(UPDATE_OP)){
        operation = Operation.UPDATE;
      } else if (line.hasOption(TABLE_OP)) {
        operation = Operation.TABLE_OP;
      } else if (line.hasOption(PERMISSION_OP)) {
        operation = Operation.PERMISSION;
      }
      else {
        operation = Operation.DOWNLOAD;
      }

      for (String arg : REQUIRED_ARGS) {
        if (!line.hasOption(arg)) {
          throw new ParseException(arg + "is required");
        }
      }

      //Cloud Endpoint related
      String username = line.getOptionValue(USERNAME_OPT, "");
      String password = line.getOptionValue(PASSWORD_OPT, "");
      tableOp = line.getOptionValue(TABLE_OP, null);

      uploadOp = line.getOptionValue(UPLOAD_OP_OPT);

      relativeServerPath = line.getOptionValue(RELATIVE_SERVER_PATH_OPT);

      // validate fields before creating CloudEndpointInfo object
      String error = FieldsValidatorUtils.checkLoginFields(
          line.getOptionValue(CLOUD_ENDPOINT_URL_OPT), line.getOptionValue(APP_ID_OPT),
          username, password,
          username.isEmpty() && password.isEmpty()
      );

      if (error != null) {
        DialogUtils.showError(error, false);
        // return early when validation fails
        return null;
      }

      cloudEndpointInfo = new CloudEndpointInfo(
          line.getOptionValue(CLOUD_ENDPOINT_URL_OPT), line.getOptionValue(APP_ID_OPT),
          username, password
      );

      new LoginTask(cloudEndpointInfo, false).blockingExecute();

      tableId = line.getOptionValue(TABLE_ID_OPT);

      if (operation == Operation.DOWNLOAD) {
        //CSV options
        downloadAttachment = line.hasOption(ATTACHMENT_OPT_SHORT);
        scanFormatting = line.hasOption(SCAN_OPT_SHORT);
        extraMetadata = line.hasOption(EXTRA_OPT_SHORT);
      }

      path = line.getOptionValue(PATH_OPT, FileUtils.getDefaultSavePath().toString());

      updateLogPath = line.getOptionValue(UPDATE_LOG_PATH_OPT);

      version = line.getOptionValue(DATA_VERSION_OPT,DEFAULT_DATA_VERSION);

      force = line.hasOption(FORCE_OPT_SHORT);
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      DialogUtils.showError(BAD_URL, false);
      e.printStackTrace();
    }

    return operation;
  }
}
