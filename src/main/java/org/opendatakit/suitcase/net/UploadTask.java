package org.opendatakit.suitcase.net;

import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.ui.DialogUtils;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.zip.DataFormatException;

import static org.opendatakit.suitcase.ui.MessageString.*;

public class UploadTask extends SuitcaseSwingWorker<Void> {
  public static final String IN_PROGRESS_STRING = "Uploading...";
  private static final int PUSH_FINISH_WAIT = 5000;

  private CloudEndpointInfo cloudEndpointInfo;
  private String operation;
  private String dataPath;
  private String relativeServerPath;
  private String version;
  private boolean isGUI;
  
  public final static String FILE_OP = "FILE";
  public final static String RESET_APP_OP = "RESET_APP";

  public UploadTask(CloudEndpointInfo cloudEndpointInfo, String dataPath, String version, boolean isGUI,
                    String operation, String relativeServerPath) {
    
    super();

    this.cloudEndpointInfo = cloudEndpointInfo;
    this.dataPath = dataPath;
    this.version = version;
    this.operation = operation;
    this.relativeServerPath = relativeServerPath;
    this.isGUI = isGUI;
  }

  @Override
  protected Void doInBackground() throws JSONException, IOException, 
    DataFormatException, InterruptedException {
    setString(IN_PROGRESS_STRING);

    SyncWrapper syncWrapper = SyncWrapper.getInstance();
    syncWrapper.updateTableList();
    
    String className = this.getClass().getSimpleName();
    if (cloudEndpointInfo == null) {
      System.out.println("cloudEndpointInfo must be specified " + className);
      return null;
    }
    
    if (dataPath == null || dataPath.length() == 0) {
      System.out.println("dataPath must be specified for " + className);
      return null;
    }
    
    if (version == null || version.length() == 0) {
      System.out.println("version must be specified for " + className);
      return null;
    }

    String op = RESET_APP_OP;
    if (operation != null) {
      op = operation.toUpperCase();
      if (op == null) {
        op = RESET_APP_OP;
      }
    }

    
    switch(op) {
    case FILE_OP:
      if (relativeServerPath == null || relativeServerPath.length() == 0) {
        System.out.println("relativeServerPath must be specified for " + FILE_OP + " op in " + className);
      }
      syncWrapper.putFile(dataPath, relativeServerPath, version);
      break;
      
    case RESET_APP_OP:
      syncWrapper.pushAllData(dataPath, version);
      break;
   
    default:
      System.out.println("You have provided an invalid uploadOp to the UploadTask");
      return null;
    }
    

    Thread.sleep(PUSH_FINISH_WAIT);
    syncWrapper.updateTableList();

    return null;
  }

  @Override
  protected void finished() {
    try {
      get();

      setString(SuitcaseProgressBar.PB_DONE);
    } catch (InterruptedException e) {
      e.printStackTrace();
      DialogUtils.showError(GENERIC_ERR, isGUI);
      setString(SuitcaseProgressBar.PB_ERROR);
      returnCode = SuitcaseSwingWorker.errorCode;
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();

      String errMsg;
      if (cause instanceof IOException) {
        errMsg = IO_READ_ERR;
      } else if (cause instanceof JSONException) {
        errMsg = VISIT_WEB_ERROR;
      } else if (cause instanceof DataFormatException) {
        errMsg = INVALID_CSV;
      } else {
        errMsg = GENERIC_ERR;
      }

      DialogUtils.showError(errMsg, isGUI);
      setString(SuitcaseProgressBar.PB_ERROR);
      cause.printStackTrace();
      returnCode = SuitcaseSwingWorker.errorCode;  
    } finally {
      setIndeterminate(false);
    }
  }
}
