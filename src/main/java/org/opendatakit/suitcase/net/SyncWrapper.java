package org.opendatakit.suitcase.net;

import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.opendatakit.aggregate.odktables.rest.entity.PrivilegesInfo;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.sync.client.SyncClient;
import org.opendatakit.sync.data.ColumnDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.zip.DataFormatException;

import static org.opendatakit.sync.client.SyncClient.*;

public class SyncWrapper {
  
  private static final String FETCH_LIMIT = "1000";
  private static final int DELETE_TABLE_DEF_WAIT = 1000;
  private static final int PUSH_DONE_WAIT = 5000;

  private CloudEndpointInfo cloudEndpointInfo;
  private SyncClient sc;
  private boolean hasInit;

  private SyncWrapper() {
    this.hasInit = false;
  }

  private static class InstanceHolder {
    private static final SyncWrapper INSTANCE = new SyncWrapper();
  }

  public static SyncWrapper getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public void init(CloudEndpointInfo cloudEndpointInfo) throws IOException {
    if (!hasInit) {
      this.cloudEndpointInfo = cloudEndpointInfo;
      this.sc = new SyncClient();
      
      String cloud_endpoint_url = cloudEndpointInfo.getHostUrl();
      if (cloud_endpoint_url.endsWith("/")) {
        cloud_endpoint_url = cloud_endpoint_url.substring(0, cloud_endpoint_url.length()-1);
      }
      
      URL url = new URL(cloud_endpoint_url);
      String host = url.getHost();

      this.sc.init(host, this.cloudEndpointInfo.getUserName(), this.cloudEndpointInfo.getPassword());

      hasInit = true;
    }
  }

  public void reset() {
    this.hasInit = false;
  }

  public boolean isInitialized() {
    return hasInit;
  }

  public Set<String> updateTableList() throws IOException, JSONException {
  	cloudEndpointInfo.resetTableIdSchemaETag();
  	
    JSONArray tables =
        sc.getTables(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId()).getJSONArray(TABLES_JSON);

    for (int i = 0; i < tables.size(); i++) {
      String tableId = tables.getJSONObject(i).getString(TABLE_ID_JSON);
      String eTag = tables.getJSONObject(i).getString(SCHEMA_ETAG_JSON);
      cloudEndpointInfo.addTableId(tableId, eTag);
    }

    return cloudEndpointInfo.getAllTableId();
  }

  public void pushAllData(String dataPath, String version)
      throws JSONException, IOException, DataFormatException {
//    System.out.println("pushAllData " + cloudEndpointInfo.getServerUrl() + " " +cloudEndpointInfo.getAppId() + " " + dataPath +" "+version);
    sc.pushAllDataToUri(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), dataPath, version);

    try {
      Thread.sleep(PUSH_DONE_WAIT);
    } catch (InterruptedException e) {
      //ignore
    }
  }
  
  public int putFile(String dataPath, String relativePathOnServer, String version) throws IOException {
    return sc.uploadFile(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(),
        dataPath, relativePathOnServer, version);
  }

  public int deleteFile(String filename, String version) throws IOException {
    return sc.deleteFile(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), filename, version);
  }

  public JSONObject getManifestForAppLevelFiles(String version) throws IOException, JSONException {
    return sc.getManifestForAppLevelFiles(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), version);
  }
  
  public int createTable(String tableId, String csvFilePath) {
    if (tableId == null || tableId.length() == 0) {
      throw new IllegalArgumentException("createTable: tableId cannot be null");
    }

    if (csvFilePath == null || csvFilePath.length() == 0) {
      throw new IllegalArgumentException("createTable: CSV file path cannot be null");
    }

    File csvFile = new File(csvFilePath);
    if (!csvFile.exists() || !csvFile.isFile()) {
      throw new IllegalArgumentException("createTable: CSV file must exist and be a valid file");
    }

    try {
      sc.createTableWithCSV(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId, null, csvFilePath);
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    } catch (DataFormatException dfe) {
      dfe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (JSONException je) {
      je.printStackTrace();
    }

    return 0;
  }

  public int uploadPermissionCSV(String csvFilePath) throws IOException {
    int rspCode = 0;
    
    if (csvFilePath == null || csvFilePath.length() == 0) {
      throw new IllegalArgumentException("uploadPermissionCSV: CSV file path cannot be null");
    }

    File csvFile = new File(csvFilePath);
    if (!csvFile.exists() || !csvFile.isFile()) {
      throw new IllegalArgumentException("uploadPermissionCSV: CSV file must exist and be a valid file");
    }

    try {
      String cloud_endpoint_url = cloudEndpointInfo.getHostUrl();
      if (cloud_endpoint_url.endsWith("/")) {
        cloud_endpoint_url = cloud_endpoint_url.substring(0, cloud_endpoint_url.length() - 1);
      }
      rspCode = sc.uploadPermissionCSV(cloudEndpointInfo.getHostUrl(), cloudEndpointInfo.getAppId(), csvFilePath);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } 

    return rspCode;
  }

  public int deleteTableDefinition(String tableId) throws IOException {
    return deleteTableDefinition(tableId, null);
  }
  
  public JSONObject getTableDefinition(String tableId) throws ClientProtocolException, 
      IOException, JSONException {
    JSONObject tableDef = null;
    
    if (tableId == null || tableId.length() == 0) {
      throw new IllegalArgumentException("tableId cannot be null");
    }
    
    if (!cloudEndpointInfo.tableIdExists(tableId)) {
      throw new IllegalArgumentException("tableId: " + tableId + " does not exist");
    }
    
    String schemaETag = cloudEndpointInfo.getSchemaETag(tableId);
    
    tableDef = sc.getTableDefinition(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId, schemaETag);
   
    return tableDef;
  }

  public int deleteTableDefinition(String tableId, String schemaETag) throws IOException {
    if (tableId == null || tableId.length() == 0) {
      throw new IllegalArgumentException("tableId cannot be null");
    }
    
    if (schemaETag == null && !cloudEndpointInfo.tableIdExists(tableId)) {
      throw new IllegalArgumentException("tableId: " + tableId + " does not exist");
    }

    if (schemaETag == null) {
      schemaETag = cloudEndpointInfo.getSchemaETag(tableId);
    }

    int result =
        sc.deleteTableDefinition(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId, schemaETag);

    try {
      Thread.sleep(DELETE_TABLE_DEF_WAIT);
    } catch (InterruptedException e) {
      //ignore
    }
    return result;
  }

  public JSONObject getManifestForRow(String tableId, String rowId)
      throws IOException, JSONException {
    if (!cloudEndpointInfo.tableIdExists(tableId)) {
      throw new IllegalArgumentException("tableId: " + tableId + " does not exist");
    }
    
    String schemaETag = verifyTableIdAndSchemaETag(tableId);

    return sc.getManifestForRow(
        cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId,
        schemaETag, rowId);
  }

  public JSONObject getRows(String tableId, String cursor) throws IOException, JSONException {
    if (!cloudEndpointInfo.tableIdExists(tableId)) {
      throw new IllegalArgumentException("tableId: " + tableId + " does not exist");
    }
    
    String schemaETag = verifyTableIdAndSchemaETag(tableId);

    return sc.getRows(
        cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId, schemaETag,
        cursor, FETCH_LIMIT
    );
  }

  public void getFileForRow(String tableId, String rowId, String savePath, String relPathOnServer)
      throws IOException, JSONException {
    if (!cloudEndpointInfo.tableIdExists(tableId)) {
      throw new IllegalArgumentException("tableId: " + tableId + " does not exist");
    }
    
    String schemaETag = verifyTableIdAndSchemaETag(tableId);

    sc.getFileForRow(
        cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId, schemaETag,
        rowId, false, savePath, relPathOnServer);
  }

  public void batchGetFilesForRow(String tableId, String rowId, String savePath, JSONObject files)
      throws IOException, JSONException {
    if (!cloudEndpointInfo.tableIdExists(tableId)) {
      throw new IllegalArgumentException("tableId: " + tableId + " does not exist");
    }

    String schemaETag = verifyTableIdAndSchemaETag(tableId);
    
    sc.batchGetFilesForRow(
        cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId, schemaETag,
        rowId, savePath, files, -1);
  }
  
  public String getDataETag(String tableId, String tableSchemaETag) {
    String dataETag = null;
    
    try{
      JSONObject res = sc.getRowsSince(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId, tableSchemaETag, null, null,
          null);
      
      if (res.containsKey(DATA_ETAG_JSON) && !res.isNull(DATA_ETAG_JSON)) {
        dataETag = res.getString(DATA_ETAG_JSON);
      }
      
    } catch (ClientProtocolException cpe) {
      cpe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (JSONException je) {
      je.printStackTrace();
    }  

    return dataETag;
  }
  
  public RowOutcomeList alterRowsUsingSingleBatch(String tableId, ArrayList<Row> rowArrayList) 
      throws ClientProtocolException, IOException, JSONException {    
    if (tableId == null || tableId.length() == 0) {
      throw new IllegalArgumentException("writeRowDataToCSV: tableId cannot be empty");
    }
    
    String schemaETag = verifyTableIdAndSchemaETag(tableId);
    
    String dataETag = getDataETag(tableId, schemaETag);
    
    return sc.alterRowsUsingSingleBatch(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId, schemaETag, dataETag, rowArrayList);
  }
  
  public void deleteRowsUsingBulkUpload(String tableId, ArrayList<Row> rowArrayList) 
      throws ClientProtocolException, IOException, JSONException {
    if (tableId == null || tableId.length() == 0) {
      throw new IllegalArgumentException("deleteRowsUsingBulkUpload: tableId must not be null");
    }
    
    String schemaETag = verifyTableIdAndSchemaETag(tableId);
    
    String dataETag = getDataETag(tableId, schemaETag);
    
    sc.deleteRowsUsingBulkUpload(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableId,
        schemaETag, dataETag, rowArrayList, 0);
    
  }
  
  String verifyTableIdAndSchemaETag(String tableId) throws IOException, JSONException {
    String schemaETag = null;
    
    if (tableId == null || tableId.length() == 0) {
      throw new IllegalArgumentException("verifyTableIdAndSchemaETag: tableId must not be null");
    }
    
    if (!cloudEndpointInfo.tableIdExists(tableId)) {
      // Update the list in case things have changed
      updateTableList();
      
      if (cloudEndpointInfo.tableIdExists(tableId)) {
        throw new IllegalArgumentException("verifyTableIdAndSchemaETag: tableId does not exist on server");
      }
    }
    
    schemaETag = cloudEndpointInfo.getSchemaETag(tableId);
    if (schemaETag == null || schemaETag.length() == 0) {
      // Update the list to make sure that we have the most recent
      updateTableList();
      
      schemaETag = cloudEndpointInfo.getSchemaETag(tableId);
      if (schemaETag == null || schemaETag.length() == 0) {
        throw new IllegalArgumentException("verifyTableIdAndSchemaETag: schemaETag does not exist on server");
      }
    }
    
    return schemaETag;
  }
  
  public ArrayList<ColumnDefinition> buildColumnDefinitions(String tableId) 
    throws JSONException, IOException {
    
    ArrayList<ColumnDefinition> colDefs = null;
    
    if (tableId == null || tableId.length() == 0) {
      throw new IllegalArgumentException("buildColumnDefinitions: tableId must not be null");
    }
    
    if (!cloudEndpointInfo.tableIdExists(tableId)) {
      // Update the list in case things have changed
      updateTableList();
      
      if (cloudEndpointInfo.tableIdExists(tableId)) {
        throw new IllegalArgumentException("buildColumnDefinitions: tableId does not exist on server");
      }
    }
    
    JSONObject tableDefObj = this.getTableDefinition(tableId);
    
    ObjectMapper mapper = new ObjectMapper();
    TableDefinitionResource tableDefRes = mapper.readValue(tableDefObj.toString(), TableDefinitionResource.class);
    
    colDefs = ColumnDefinition.buildColumnDefinitions(cloudEndpointInfo.getAppId(),
        tableId, tableDefRes.getColumns());
    
    return colDefs;
  }
  
  public void setPrivilegesInfo() throws ClientProtocolException, IOException,
      JSONException {

    PrivilegesInfo privInfo = sc.getPrivilegesInfo(cloudEndpointInfo.getServerUrl(),
        cloudEndpointInfo.getAppId());
    
    if (privInfo != null) {
      cloudEndpointInfo.setPrivilegedUserName(privInfo.getUser_id());
    }
  }
  
  @Override
  protected Object clone() throws CloneNotSupportedException {
    super.clone();
    throw new CloneNotSupportedException();
  }
}
