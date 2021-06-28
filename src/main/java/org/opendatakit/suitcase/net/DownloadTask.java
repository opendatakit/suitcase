package org.opendatakit.suitcase.net;

import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.model.CsvConfig;
import org.opendatakit.suitcase.model.ODKCsv;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvWriter;
import org.opendatakit.suitcase.model.ScanJsonException;
import org.opendatakit.sync.client.SyncClient;
import org.opendatakit.suitcase.ui.DialogUtils;
import org.opendatakit.suitcase.ui.ProgressBarStatus;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;
import org.opendatakit.suitcase.utils.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;

import static org.opendatakit.suitcase.ui.MessageString.*;

public class DownloadTask extends SuitcaseSwingWorker<Void> {
  private static final String RETRIEVING_ROW = "Retrieving rows";
  private static final String PROCESSING_ROW = "Processing and writing data";

  private CloudEndpointInfo cloudEndpointInfo;
  private ODKCsv csv;
  private CsvConfig csvConfig;
  private String savePath;
  private boolean isGUI;

  public DownloadTask(CloudEndpointInfo cloudEndpointInfo, ODKCsv csv, CsvConfig csvConfig, String savePath,
                      boolean isGUI) {
    super();

    this.cloudEndpointInfo = cloudEndpointInfo;
    this.csv = csv;
    this.csvConfig = csvConfig;
    this.savePath = savePath;
    this.isGUI = isGUI;
  }

  @Override
  protected Void doInBackground() throws IOException, JSONException, ScanJsonException {
    //assume csv has already been initialized by caller of this worker

    // check existing data, skip check for CLI
    if (FileUtils.isDownloaded(cloudEndpointInfo, csv.getTableId(), csvConfig, savePath) &&
        DialogUtils.promptConfirm(OVERWRITE_CSV, isGUI, !isGUI)) {
      FileUtils.deleteCsv(cloudEndpointInfo, csvConfig, csv.getTableId(), savePath);
    }

    // then create directory structure when needed
    FileUtils.createDirectory(cloudEndpointInfo, csvConfig, csv.getTableId(), savePath);

    SyncWrapper syncWrapper = SyncWrapper.getInstance();

    // retrieve data from Cloud Endpoint and store in csv
    if (csv.getSize() == 0) {
      publish(new ProgressBarStatus(0, RETRIEVING_ROW, true));

      JSONObject rows;
      String cursor = null;

      do {
        rows = syncWrapper.getRows(csv.getTableId(), cursor);
        cursor = rows.optString(SyncClient.WEB_SAFE_RESUME_CURSOR_JSON);
        csv.tryAdd(rows.getJSONArray(SyncClient.ROWS_STR_JSON));
      } while (rows.getBoolean(SyncClient.HAS_MORE_RESULTS_JSON));
    }

    // write out csv to file
    publish(new ProgressBarStatus(0, PROCESSING_ROW, false));
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
      } else if (cause instanceof ScanJsonException) {
        errMsg = SCAN_FORMATTING_ERROR;
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
