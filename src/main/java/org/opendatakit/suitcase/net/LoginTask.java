package org.opendatakit.suitcase.net;

import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.model.SyncClientError;
import org.opendatakit.suitcase.ui.DialogUtils;
import org.opendatakit.suitcase.ui.ProgressBarStatus;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.opendatakit.suitcase.ui.MessageString.*;

public class LoginTask extends SuitcaseSwingWorker<Void> {
  private static final String UPDATING_TABLES_LIST = "Updating Tables List";
  private static final String UPDATING_PRIVILEGES_LIST = "Updating Privileges";
  private CloudEndpointInfo cloudEndpointInfo;
  private boolean isGUI;

  public LoginTask(CloudEndpointInfo cloudEndpointInfo, boolean isGUI) {
    super();

    this.cloudEndpointInfo = cloudEndpointInfo;
    this.isGUI = isGUI;
  }

  @Override
  protected Void doInBackground() throws IOException, JSONException {
    SyncWrapper syncWrapper = SyncWrapper.getInstance();

    syncWrapper.reset();
    syncWrapper.init(cloudEndpointInfo);
    if(syncWrapper.isInitialized()) {
      publish(new ProgressBarStatus(0, UPDATING_TABLES_LIST, false));
      syncWrapper.updateTableList();
      publish(new ProgressBarStatus(0, UPDATING_PRIVILEGES_LIST, false));
      syncWrapper.setPrivilegesInfo();
    }
    return null;
  }

  @Override
  protected void finished() {
    try {
      get();

      setString(SuitcaseProgressBar.PB_IDLE);
      setIndeterminate(false);
    } catch (InterruptedException e) {
      e.printStackTrace();
      DialogUtils.showError(GENERIC_ERR, isGUI);
      returnCode = SuitcaseSwingWorker.errorCode;
      SyncWrapper.getInstance().reset();
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();

      String errMsg;
      if (cause instanceof JSONException) {
        SyncClientError syncClientError = new SyncClientError(e);
        if(syncClientError.getStatusCode() == 401){
          errMsg = BAD_CRED;
        }
        else {
          errMsg = syncClientError.getMessage();
        }
        setError(errMsg);                            // call in case of bad credentials to logout user if the user is logged in via save credentials
      } else if (cause instanceof IOException) {
        errMsg = HTTP_IO_ERROR;
      } else {
        errMsg = GENERIC_ERR;
      }

      DialogUtils.showError(errMsg, isGUI);
      cause.printStackTrace();
      returnCode = SuitcaseSwingWorker.errorCode;
      SyncWrapper.getInstance().reset();
    }
  }
}
