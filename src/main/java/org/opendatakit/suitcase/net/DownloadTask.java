package org.opendatakit.suitcase.net;

import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.model.CsvConfig;
import org.opendatakit.suitcase.model.ODKCsv;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvWriter;
import org.opendatakit.sync.client.SyncClient;
import org.opendatakit.suitcase.ui.DialogUtils;
import org.opendatakit.suitcase.ui.ProgressBarStatus;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;
import org.opendatakit.suitcase.utils.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.opendatakit.suitcase.ui.MessageString.*;

public class DownloadTask extends SuitcaseSwingWorker<Void> {
  private static final String RETRIEVING_ROW_PREFIX = "Retrieving rows for ";
  private static final String PROCESSING_ROW_PREFIX = "Processing and writing data for ";

  private CloudEndpointInfo cloudEndpointInfo;
  private CsvConfig csvConfig;
  private String savePath;
  private List<String> tableIds;
  private boolean isGUI;

  public DownloadTask(CloudEndpointInfo cloudEndpointInfo, List<String> tableIds, CsvConfig csvConfig, String savePath,
                      boolean isGUI) {
    super();
    this.cloudEndpointInfo = cloudEndpointInfo;
    this.tableIds = tableIds;
    this.csvConfig = csvConfig;
    this.savePath = savePath;
    this.isGUI = isGUI;
  }

  @Override
  protected Void doInBackground() throws IOException, JSONException {

    // check existing data, skip check for CLI

    for(String tableId:tableIds){
      // create a new attachment manager for every table id
      AttachmentManager attachMngr = new AttachmentManager(cloudEndpointInfo, tableId, savePath);
      // create a new ODKCsv for every table id
      ODKCsv csv = new ODKCsv(attachMngr,cloudEndpointInfo,tableId);


    // then create directory structure when needed
    FileUtils.createDirectory(cloudEndpointInfo, csvConfig, csv.getTableId(), savePath);

    SyncWrapper syncWrapper = SyncWrapper.getInstance();

    // retrieve data from Cloud Endpoint and store in csv
    if (csv.getSize() == 0) {
      publish(new ProgressBarStatus(0, RETRIEVING_ROW_PREFIX+tableId, true));

      JSONObject rows;
      String cursor = null;

      do {
        rows = syncWrapper.getRows(csv.getTableId(), cursor);
        cursor = rows.optString(SyncClient.WEB_SAFE_RESUME_CURSOR_JSON);
        csv.tryAdd(rows.getJSONArray(SyncClient.ROWS_STR_JSON));
      } while (rows.getBoolean(SyncClient.HAS_MORE_RESULTS_JSON));
    }

    // write out csv to file
    publish(new ProgressBarStatus(0, PROCESSING_ROW_PREFIX + tableId, false));
    RFC4180CsvWriter csvWriter = null;
    try {
      csvWriter = new RFC4180CsvWriter(Files.newBufferedWriter(
              FileUtils.getCSVPath(cloudEndpointInfo, csv.getTableId(), csvConfig, savePath),
              StandardCharsets.UTF_8,
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING
      ));

      //Write header then rows
      csvWriter.writeNext(csv.getHeader(csvConfig));

      ODKCsv.ODKCSVIterator csvIt = csv.getODKCSVIterator();
      while (csvIt.hasNext()) {
        csvWriter.writeNext(csvIt.next(csvConfig));

        //Set value of progress bar with percentage of rows done
        int progress = (int) ((double) csvIt.getIndex() / csv.getSize() * 100);
        publish(new ProgressBarStatus(progress, null, null));
      }
    } finally {
      if (csvWriter != null) {
        csvWriter.close();
      }
    }
  }

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
        errMsg = IO_WRITE_ERR;
      } else if (cause instanceof JSONException) {
        errMsg = VISIT_WEB_ERROR;
      } else {
        errMsg = GENERIC_ERR;
      }

      cause.printStackTrace();
      DialogUtils.showError(errMsg, isGUI);
      setString(SuitcaseProgressBar.PB_ERROR);
      returnCode = SuitcaseSwingWorker.errorCode;
    } finally {
      setIndeterminate(false);
    }
  }
}
