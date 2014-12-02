/*
 * Copyright 2014 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kuali.test.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.eclipse.jetty.http.HttpHeaders;
import org.kuali.test.creator.TestCreator;


public class Constants {
    public static final String DEFAULT_TIMESTAMP_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT_STRING = "yyyy-MM-dd";
    public static final String SELECT_DATETIME_FORMAT_STRING = "MM/dd/yyyy HH:mm";
    
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0";
    public static final int MILLIS_PER_MINUTE = 1000 * 60;
    
    public static final SimpleDateFormat FILENAME_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    public static final SimpleDateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT_STRING);
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STRING);
    
    public static final ImageIcon KUALI_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/kuali-small.png"));
    
    public static final ImageIcon REPOSITORY_ICON = new ImageIcon(TestCreator.class.getResource("/images/repository.png"));
    public static final ImageIcon PLATFORM_ICON = new ImageIcon(TestCreator.class.getResource("/images/platform.png"));
    public static final ImageIcon PLATFORM_TOOLBAR_ICON = new ImageIcon(TestCreator.class.getResource("/images/platform-toolbar.png"));
    public static final ImageIcon TEST_SUITE_ICON = new ImageIcon(TestCreator.class.getResource("/images/test-suite.png"));
    public static final ImageIcon TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/test.png"));
    public static final ImageIcon SAVE_CONFIGURATION_ICON = new ImageIcon(TestCreator.class.getResource("/images/save.png"));
    public static final ImageIcon CREATE_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/createtest.png"));
    public static final ImageIcon CANCEL_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/cancel-test.png"));
    public static final ImageIcon EXIT_APPLICATION_TOOLBAR_ICON = new ImageIcon(TestCreator.class.getResource("/images/exit-toolbar.png"));

    public static final ImageIcon START_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/start-test.png"));
    public static final ImageIcon CREATE_CHECKPOINT_ICON = new ImageIcon(TestCreator.class.getResource("/images/create-checkpoint.png"));
    public static final ImageIcon SAVE_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/save-test.png"));
    public static final ImageIcon CLOCK_ICON = new ImageIcon(TestCreator.class.getResource("/images/clock.png"));
    public static final ImageIcon CLOSE_TAB_ICON = new ImageIcon(TestCreator.class.getResource("/images/close-tab.png"));
    public static final ImageIcon ADD_ICON = new ImageIcon(TestCreator.class.getResource("/images/add.png"));
    public static final ImageIcon DELETE_ICON = new ImageIcon(TestCreator.class.getResource("/images/delete.png"));
    public static final ImageIcon DETAILS_ICON = new ImageIcon(TestCreator.class.getResource("/images/details.png"));

    public static final ImageIcon DATABASE_ICON = new ImageIcon(TestCreator.class.getResource("/images/database.png"));
    public static final ImageIcon DATABASE_TOOLBAR_ICON = new ImageIcon(TestCreator.class.getResource("/images/database-toolbar.png"));
    public static final ImageIcon DATABASE_SETTING_ICON = new ImageIcon(TestCreator.class.getResource("/images/database-setting.png"));
    public static final ImageIcon DATABASE_TABLE_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbtable.png"));
    public static final ImageIcon DATABASE_TABLE_OUTER_JOIN_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbtable-outer-join.png"));
    public static final ImageIcon DATABASE_COLUMN_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbcolumn.png"));
    public static final ImageIcon DATABASE_PKCOLUMN_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbpkcolumn.png"));
    public static final ImageIcon DATABASE_COLUMN_SELECTED_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbcolumnsel.png"));
    public static final ImageIcon DATABASE_PKCOLUMN_SELECTED_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbpkcolumnsel.png"));

    public static final ImageIcon WEB_SERVICE_ICON = new ImageIcon(TestCreator.class.getResource("/images/webservice.png"));
    public static final ImageIcon WEB_SERVICE_TOOLBAR_ICON = new ImageIcon(TestCreator.class.getResource("/images/webservice-toolbar.png"));
    public static final ImageIcon WEB_SERVICE_SETTING_ICON = new ImageIcon(TestCreator.class.getResource("/images/webservice-setting.png"));
    public static final ImageIcon FILE_SEARCH_ICON = new ImageIcon(TestCreator.class.getResource("/images/file-search.png"));
    public static final ImageIcon JMX_CONNECTION_ICON = new ImageIcon(TestCreator.class.getResource("/images/jmxconnection.png"));
    public static final ImageIcon JMX_CONNECTION_TOOLBAR_ICON = new ImageIcon(TestCreator.class.getResource("/images/jmxconnection-toolbar.png"));
    public static final ImageIcon JMX_CONNECTION_SETTING_ICON = new ImageIcon(TestCreator.class.getResource("/images/jmxconnection-setting.png"));
    public static final ImageIcon SCHEDULE_TEST_TOOLBAR_ICON = new ImageIcon(TestCreator.class.getResource("/images/schedule-test-toolbar.png"));
    public static final ImageIcon ADD_PARAM_ICON = new ImageIcon(TestCreator.class.getResource("/images/add-param.png"));
    public static final ImageIcon SET_PARAM_ICON = new ImageIcon(TestCreator.class.getResource("/images/set-param.png"));
    public static final ImageIcon REFRESH_BROWSER_ICON = new ImageIcon(TestCreator.class.getResource("/images/refresh-browser.png"));
    public static final ImageIcon LOADING_SPINNER_ICON = new ImageIcon(TestCreator.class.getResource("/images/loading-spinner.gif"));

    
    public static final int DEFAULT_HORIZONTAL_DIVIDER_LOCATION = 150;
    public static final int DEFAULT_VERTICAL_DIVIDER_LOCATION = 300;
    public static final int DEFAULT_HTTP_RESPONSE_BUFFER_SIZE = 1024;
    public static final int MAINFRAME_DEFAULT_LEFT = 100;
    public static final int MAINFRAME_DEFAULT_TOP = 100;
    public static final int MAINFRAME_DEFAULT_WIDTH = 800;
    public static final int MAINFRAME_DEFAULT_HEIGHT = 600;
    public static final int TEST_OPERATION_POPUP_X = 150;
    public static final int TEST_OPERATION_POPUP_Y= 75;

    public static final int MAX_TABLE_RELATIONSHIP_DEPTH = 6;
    
    public static final String KUALI_TEST_TITLE = "Kuali Test Framework";
    public static final String PREFS_ROOT_NODE = "kualitest/mainframe";
    public static final String LOCAL_RUN_EMAIL = "local-run-email";
    public static final String PREFS_TABLE_NODE = "kualitest/tables";
    public static final String PREFS_DLG_NODE = "kualitest/dialogs";
    public static final String PREFS_FILES_NODE = "kualitest/files";
    public static final String PREFS_HORIZONTAL_DIVIDER_LOCATION = "horizontal-divider-location";
    public static final String PREFS_VERTICAL_DIVIDER_LOCATION = "vertical-divider-location";
    public static final String PREFS_MAINFRAME_LEFT = "mainframe-left";
    public static final String PREFS_MAINFRAME_TOP = "mainframe-top";
    public static final String PREFS_MAINFRAME_WIDTH = "mainframe-width";
    public static final String PREFS_MAINFRAME_HEIGHT = "mainframe-height";
    public static final String PREFS_MAINFRAME_WINDOW_STATE = "mainframe-window-state";
    public static final String COLOR_DARK_RED = "#680000";
    public static final String COLOR_DARK_BLUE = "#000099";
    public static final String COLOR_DARK_GREEN = "#006600";
    public static final String CONTENT_TYPE_KEY = "Content-Type";
    public static final String PERCENT_CHARACTER = "%";
    public static final String URL_ENCODED_PERCENT_CHARACTER = "%25";
    public static final String REPOSITORY_ROOT_REPLACE = "${repository-location}";
    
    public static final int DEFAULT_DIALOG_LEFT = 300;
    public static final int DEFAULT_DIALOG_TOP = 200;
    public static final String PREFS_DLG_LEFT = "-dlg-left";
    public static final String PREFS_DLG_TOP = "-dlg-top";
    public static final String PREFS_DLG_WIDTH = "-dlg-width";
    public static final String PREFS_DLG_HEIGHT = "-dlg-height";
    
    
    public static final String TEST_TRANSFER_MIME_TYPE = "test-transfer";
    public static final String PREFS_LAST_FILE_TEST_DIR = "last-file-test-dir";
    
    public static final String SAVE_CONFIGURATION = "Save Configuration";
    public static final String CREATE_TEST = "Create Test";
    public static final String NEW_WEB_TEST = "New Web Test";
    public static final String TEST_OUTPUT = "Test Output";
    public static final String RUNNING_TESTS = "Running Tests";
    public static final String REPOSITORY = "Repository";
    public static final String DATABASES = "Databases";
    public static final String WEBSERVICES = "Web Services";
    public static final String JMX = "JMX Connections";
    
    public static final String SAVE_ACTION = "Save";
    public static final String RUN_ACTION = "Run";
    public static final String OK_ACTION = "Ok";
    public static final String CANCEL_ACTION = "Cancel";
    public static final String CLOSE_ACTION = "Close";
    public static final String CONTINUE_ACTION = "Continue";
    public static final String START_TEST_ACTION = "Start Test";
    public static final String UPDATE_ACTION = "Update";
    public static final String SELECT_ACTION = "Select";
    public static final String CANCEL_TEST_ACTION = "Cancel Test";
    public static final String CREATE_CHECKPOINT_ACTION = "Create Checkpoint";
    public static final String VIEW_CHECKPOINTS_ACTION = "View Checkpoints";
    public static final String CREATE_PARAMETER_ACTION = "Create Parameter";
    public static final String VIEW_PARAMETERS_ACTION = "View Parameters";
    public static final String CREATE_COMMENT_ACTION = "Create Comment";
    public static final String VIEW_COMMENTS_ACTION = "View Comments";
    public static final String CREATE_RANDOM_LIST_SELECTION_ACTION = "Create Random List Selection";
    public static final String ADD_ACTION = "Add";
    public static final String OPERATION_ACTION = "Operation";
    public static final String COPY_ACTION = "Copy";
    public static final String DELETE_ACTION = "Delete";
    public static final String REFRESH_BROWSER_ACTION = "Refresh";
    public static final String SAVE_TEST_ACTION = "Save Test";
    public static final String NEW_BROWSER_TAB_DEFAULT_TEXT = "new browser tab...";
    public static final String SCHEDULE_TEST_ACTION = "Schedule Test";
    public static final String REMOVE_TEST_ACTION = "Remove Test";
    public static final String REMOVE_PARAMETER_ACTION = "Remove Parameter";
    public static final String EXECUTION_PARAMETER_ACTION = "Parameter";
    public static final String ADD_PARAM_ACTION = "Add Param";
    public static final String SET_PARAM_ACTION = "Set Param";
    public static final String PLATFORM_SELECTED_ACTION = "platform-selected";
    public static final String SAVE_AND_RUN_ACTION = "Save and Run";
    public static final String DEFAULT_PROXY_HOST= "localhost";
    public static final String DEFAULT_PROXY_PORT= "8888";
    public static final int MAX_RESPONSE_BUFFER_SIZE = (1024 * 1024);
    public static final int MAX_REQUEST_BUFFER_SIZE = (1024 * 1024);
    public static final int DEFAULT_SPLASH_WIDTH = 350;
    public static final int DEFAULT_SPLASH_HEIGHT = 125;
    public static final int DEFAULT_TABLE_COLUMN_WIDTH = 75;
    public static final int MESSAGE_DISPLAY_WORDS_PER_LINE = 20;
    public static final int DEFAULT_DISPLAY_TABLE_ROWS = 10;
    public static final String HEADER_INFO_PANEL_NAME = "Header Info";
    public static final String HEADER_AREA = "Header Area";
    public static final String DOCUMENT_NAME = "Document Name";
    public static final String DEFAULT_HTML_PROPERTY_GROUP = "main";
    public static final String DEFAULT_HTML_PROPERTY_SECTION = "none";
    public static final String ROUTELOG_HTML_PROPERTY_GROUP = "RouteLog";
    public static final String NODE_INDEX_MATCHER_CODE = "I";
    public static final String NODE_ID = "nodeid";
    public static final String SYSTEM_PROPERTY_GROUP = "system";
    public static final String DATABASE_PROPERTY_GROUP = "database";
    public static final String WEB_SERVICE_PROPERTY_GROUP = "webservice";
    public static final String ADD_ROW_ACTION = "Add Row";
    public static final String FILE_PROPERTY_GROUP = "file";
    public static final String CONTAINING_TEXT = "containing-text";
    public static final String SQL_QUERY = "sql-query";
    public static final String SAVE_QUERY_RESULTS = "save-query-results";
    public static final String NO_TEST_SUITE_NAME = "no-test-suite";
    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String UNKNOWN = "unknown";
    public static final String HTTP_HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HTTP_HEADER_ACCEPT_LANGUAGE_US = "en-US,en;q=0.8";
    public static final String HTTP_HEADER_USER_AGENT = "User-Agent";
    public static final String HTTP_HEADER_LOCATION = "Location";
    public static final String JSESSIONID_PARAMETER_NAME = "jsessionid";
    public static final String HTTP_PROTOCOL = "http://";
    public static final String HTTPS_PROTOCOL = "https://";
    public static final String FORWARD_SLASH = "/";
    public static final String TAG_MATCH_REGEX_PATTERN = "\\<[^>]*>";
    public static final String WEB_SERVICE_OPERATION = "web-service-operation";
    public static final String EXPECTED_RESULT = "expected-result";
    public static final int HTTP_URL_REQUEST_FOR_PARAMETERS_LIST_MAX_SIZE = 3;
    public static final String SAVE_SCREEN = "save-screen";
    public static final int ELAPSED_TIME_UPDATE_INTERVAL = 5;
    public static final String MULTIPART_BOUNDARY_IDENTIFIER = "boundary=";
    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 30 * 1000;
    public static final int DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT = MILLIS_PER_MINUTE;
    public static final int DEFAULT_HTTP_READ_TIMEOUT = MILLIS_PER_MINUTE * 5;
    public static final String SCREEN_CAPTURE_DIR = "screen-captures";
    public static final String HELP_FILE_PATH = "/help/kuali-test.pdf";
    public static final String SAVE_QUERY_RESULTS_DIR = "save-query-results";
    public static final String ROW_COUNT_PROPERTY = "row-count";
    public static final String DELETE_ROW_ACTION = "Delete Row";
    public static final String ADD_COLUMN_ACTION = "Add Column";
    public static final String ADD_ALL_COLUMNS_ACTION = "Add All Columns";
    public static final String DELETE_COLUMN_ACTION = "Delete Column";
    public static final String ADD_COMPARISON_ACTION = "Add Comparison";
    public static final String ADD_NAME_ACTION = "Add Name";
    public static final String REMOVE_NAME_ACTION = "Remove Name";
    public static final String DELETE_COMPARISON_ACTION = "Delete Comparison";
    public static final String SHOW_TEST_INFORMATION_ACTION = "Show test information";
    public static final String FILE_SEARCH_ACTION = "File Search";
    public static final String FILE_DIRECTORY = "file-directory";
    public static final String FILE_NAME_PATTERN = "file-name-pattern";
    public static final String IGNORE_TABLE_DATA_INDICATOR = "^";
    public static final String MULTIPART_NAME_VALUE_SEPARATOR = "\u00A1";
    public static final String REPLACE_DATA_POSITION_HOLDER = IGNORE_TABLE_DATA_INDICATOR;
    public static final String MULTIPART_PARAMETER_SEPARATOR = "|";
    public static final String MAX_MEMORY_PERCENT = "max-memory-percent";
    public static final String PARAMETER_NAME_CONTENT = "content";
    public static final String PARAMETER_NAME_PATH = "path";
    public static final String SEPARATOR_EQUALS = "=";
    public static final String SEPARATOR_AMPERSTAND = "&";
    public static final String ENCODED_AMPERSTAND = "%26";
    public static final String SEPARATOR_SEMICOLON = ";";
    public static final String SEPARATOR_COLON = ":";
    public static final String SEPARATOR_QUESTION = "?";
    public static final String MAX_RUNTIME_PROPERTY_NAME = "max-runtime";
    public static final int LAST_RESPONSE_STACK_SIZE = 5;
    public static final int DEFAULT_TABLE_ROW_HEIGHT = 20;
    public static final int DEFAULT_TREE_ROW_HEIGHT = 32;
    public static final int DEFAULT_WEB_SERVICE_WAIT_TIME = MILLIS_PER_MINUTE * 5;
    public static final int WEB_SERVICE_SLEEP_TIME = 1000 * 2;
    public static final int INITIAL_HTML_RESPONSE_BUFFER_SIZE = 1024;
    public static final String HTTP_HEADER_COOKIE = "Cookie";
    public static final String HTTP_HEADER_SET_COOKIE = "Set-Cookie";
    public static final String TEST_OPERATION_INDEX = "test-operation-index";
    public static final long HTML_TEST_RETRY_TIMESPAN = MILLIS_PER_MINUTE;
    public static final long HTML_TEST_RETRY_SLEEP_INTERVAL = 5000;
    public static final String DEFAULT_MESSAGE_DIGEST_ALGORITHM = "SHA";
    public static final int PROXY_CONNECTION_TIMEOUT = 300000;
    public static final long LOCAL_TEST_RUN_SLEEP_TIME = 2000;
    public static final long HTML_CLIENT_WAIT_FOR_BACKGROUND_JAVASCRIPT_TIME = 30000;
    
    public static final String SOURCE_ACCOUNTING_LINE_MATCH = ".sourceAccountingLine[";
    
    public static final int SIBLING_NODE_SEARCH_DIRECTION_INVALID = Integer.MIN_VALUE;
    public static final int SIBLING_NODE_SEARCH_DIRECTION_PREVIOUS = -1;
    public static final int SIBLING_NODE_SEARCH_DIRECTION_ABSOLUTE = 0;
    public static final int SIBLING_NODE_SEARCH_DIRECTION_NEXT = 1;
    
    
    public static final String HTTP_REQUEST_METHOD_GET = "GET";
    public static final String HTTP_REQUEST_METHOD_POST = "POST";
    public static final String HTTP_REQUEST_METHOD_CONNECT = "CONNECT";
    public static final String HTTP_RESPONSE_CONTENT_TYPE = "Content-Type";
    public static final String MIME_TYPE_HTML = "text/html";
    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MIME_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String MULTIPART = "multipart/";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    
    public static final String HTML_TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String TAB_SPACES = "    ";
    public static final String HTML_LINE_BREAK = "<br />";
    public static final String HTML_BOLD_UNDERLINE_STYLE = "<span style='font-weight: 650; white-space: nowrap; text-decoration: underline;'>" + REPLACE_DATA_POSITION_HOLDER + "</span>";
    public static final String HTML_BOLD_BLUE_STYLE = "<span style='color: " + Constants.COLOR_DARK_BLUE + "; font-weight: 650; white-space: nowrap;'>" + REPLACE_DATA_POSITION_HOLDER + "</span>";
    public static final String HTML_DARK_RED_STYLE = "<span style='color: " + Constants.COLOR_DARK_RED + "; white-space: nowrap;'>^</span>";
    public static final String HTML_DARK_GREEN_STYLE = "<span style='color: " + Constants.COLOR_DARK_GREEN + "; font-weight: 650; white-space: nowrap;'>" + REPLACE_DATA_POSITION_HOLDER + "</span>";
    
    public static final String JAVASCRIPT_SUFFIX = "js";
    public static final String CSS_SUFFIX = "css";
    public static final Set <String> IMAGE_SUFFIX_SET = new HashSet<String>();
    public static final Set <String> VALID_HTTP_REQUEST_METHOD_SET = new HashSet<String>();
    public static final Set <String> DEFAULT_HTML_CONTAINER_TAGS = new HashSet<String>();
    
    public static final String HTML_TAG_TYPE_HTML = "html";
    public static final String HTML_TAG_TYPE_OPTION = "option";
    public static final String HTML_TAG_TYPE_LABEL = "label";
    public static final String HTML_TAG_TYPE_INPUT = "input";
    public static final String HTML_TAG_TYPE_FORM = "form";
    public static final String HTML_TAG_TYPE_IFRAME = "iframe";
    public static final String HTML_TAG_TYPE_TABLE = "table";
    public static final String HTML_TAG_TYPE_TR = "tr";
    public static final String HTML_TAG_TYPE_TH = "th";
    public static final String HTML_TAG_TYPE_H1 = "h1";
    public static final String HTML_TAG_TYPE_H2 = "h2";
    public static final String HTML_TAG_TYPE_H3 = "h3";
    public static final String HTML_TAG_TYPE_TD = "td";
    public static final String HTML_TAG_TYPE_TBODY = "tbody";
    public static final String HTML_TEXT_NODE_NAME = "#text";
    public static final String HTML_TAG_TYPE_DIV = "div";
    public static final String HTML_TAG_TYPE_SPAN = "span";
    public static final String HTML_TAG_TYPE_ANCHOR = "a";
    public static final String HTML_TAG_TYPE_SELECT = "select";
    public static final String HTML_TAG_TYPE_BODY = "body";
    public static final String HTML_TAG_TYPE_TEXTAREA = "textarea";
    public static final String TAG_NAME = "tag-name";
    public static final String CHILD_NODE_INDEX = "child-node-index";
    public static final String IFRAME_IDS = "iframe-ids";
    public static final String TABLE_ID = "table-id";
    public static final String TABLE_NAME = "table-name";
    public static final String FILE_ATTACHMENT_MARKER = ":fileAttachment";
    public static final String TMP_FILE_PREFIX_SEPARATOR = "_-_";
    public static final String ATTACHMENTS = "attachments";
    
    static {
        DEFAULT_HTML_CONTAINER_TAGS.add(HTML_TAG_TYPE_BODY);
        DEFAULT_HTML_CONTAINER_TAGS.add(HTML_TAG_TYPE_TABLE);
        DEFAULT_HTML_CONTAINER_TAGS.add(HTML_TAG_TYPE_TR);
        DEFAULT_HTML_CONTAINER_TAGS.add(HTML_TAG_TYPE_IFRAME);
        DEFAULT_HTML_CONTAINER_TAGS.add(HTML_TAG_TYPE_DIV);
        DEFAULT_HTML_CONTAINER_TAGS.add(HTML_TAG_TYPE_FORM);
        
        IMAGE_SUFFIX_SET.add("gif");
        IMAGE_SUFFIX_SET.add("ico");
        IMAGE_SUFFIX_SET.add("jpg");
        IMAGE_SUFFIX_SET.add("jpeg");
        IMAGE_SUFFIX_SET.add("png");

        VALID_HTTP_REQUEST_METHOD_SET.add(HTTP_REQUEST_METHOD_GET);
        VALID_HTTP_REQUEST_METHOD_SET.add(HTTP_REQUEST_METHOD_POST);
    }
    

    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String HTML_TAG_ATTRIBUTE_SRC = "src";
    public static final String HTML_TAG_ATTRIBUTE_FOR = "for";
    public static final String HTML_TAG_ATTRIBUTE_ID = "id";
    public static final String HTML_TAG_ATTRIBUTE_ROWSPAN = "rowspan";
    public static final String HTML_TAG_ATTRIBUTE_NAME = "name";
    public static final String HTML_TAG_ATTRIBUTE_SELECTED = "selected";
    public static final String HTML_TAG_ATTRIBUTE_VALUE = "value";
    public static final String HTML_TAG_ATTRIBUTE_CHECKED = "checked";
    public static final String HTML_TAG_ATTRIBUTE_CLASS = "class";
    public static final String HTML_TAG_ATTRIBUTE_TYPE = "type";
    public static final String HTML_TAG_ATTRIBUTE_HREF = "href";
    public static final String HTML_TAG_ATTRIBUTE_SUMMARY = "summary";
    public static final String HTML_INPUT_ATTRIBUTE_TYPE_RADIO = "radio";
    public static final String HTML_INPUT_ATTRIBUTE_TYPE_CHECKBOX = "checkbox";
    public static final String HTML_INPUT_ATTRIBUTE_TYPE_TEXT = "text";
    public static final String HTML_TAG_ATTRIBUTE_CLASS_DATATABLE = "datatable";
    public static final String XML_SUFFIX = ".xml";
    public static final String HTML_SUFFIX = ".html";
    public static final String DWR_SUFFIX = ".dwr";
    public static final String PDF_SUFFIX = ".pdf";
    public static final String TXT_SUFFIX = ".txt";
    
    public static final String SQL_ORDER_BY_DESC = "DESC";
    public static final String AND = "AND";

    public static final String MAX = "MAX";
    public static final String MIN = "MIN";
    public static final String SUM = "SUM";
    public static final String AVG = "AVG";
    public static final String COUNT = "COUNT";
    
    public static final String DATA_TYPE_OTHER = "other";
    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_INT = "int";
    public static final String DATA_TYPE_DATE = "date";
    public static final String DATA_TYPE_TIMESTAMP = "timestamp";
    public static final String DATA_TYPE_FLOAT = "float";
    public static final String DEFAULT_ENCRYPTION_PASSWORD = "kjashdsayr87ewr8463fjkewyr4398";
    
    public static final String TEST_RUNNER_CONFIG_FILENAME = "test-runner-config.xml";
    
    public static final int DEFAULT_TEST_RUNNER_CONFIGURATION_UPDATE_INTERVAL = 15;
    public static final int DEFAULT_TEST_RUNNER_TEST_INQUIRY_INTERVAL = 10;
    
    public static final String[] AGGREGATE_FUNCTIONS = {
        "",
        AVG,
        COUNT,
        MAX,
        MIN,
        SUM
    };

    public static final String[] ASC_DESC = {
        "",
        "ASC",
        SQL_ORDER_BY_DESC
    };

    public static final String[] AND_OR = {
        "AND",
        "OR"
    };

    public static final String[] OPEN_PARENTHESIS = {
        "",
        "(",
        "((",
        "((("
    };

    public static final String[] CLOSE_PARENTHESIS = {
        "",
        ")",
        "))",
        ")))"
    };

    
    public static final String EQUAL_TO = "=";
    public static final String NOT_EQUAL_TO = "<>";
    public static final String LESS_THAN = "<";
    public static final String LESS_THAN_OR_EQUAL_TO = "<=";
    public static final String GREATER_THAN = ">";
    public static final String GREATER_THAN_OR_EQUAL_TO = ">=";
    public static final String IN = "in";
    public static final String NOT_INT = "not in";
    public static final String BETWEEN ="between";
    public static final String LIKE = "like";
    public static final String NOT_LIKE = "not like";
    public static final String NULL = "null";
    public static final String NOT_NULL = "not null";


    public static final String[] OPERATORS = {
        EQUAL_TO,
        LESS_THAN,
        LESS_THAN_OR_EQUAL_TO,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL_TO,
        NOT_EQUAL_TO,
        IN,
        LIKE,
        NOT_LIKE,
        NULL,
        NOT_NULL
    };
    
    public static final String SINGLE_ROW_EXISTS = "single row exists";
    public static final String MULTIPLE_ROWS_EXIST = "multiple rows exist";
    public static final String NO_ROW_EXISTS = "no rows exist";
    
    
    public static final String[] SQL_CHECKPOINT_PROPERTIES = {
        SINGLE_ROW_EXISTS,
        MULTIPLE_ROWS_EXIST,
        NO_ROW_EXISTS
    };
    
    public static final String FILE_EXISTS = "File Exists";
    public static final String FILE_DOES_NOT_EXIST = "File Does Not  Exist";
    public static final String FILE_SIZE_GREATER_THAN_ZERO = "File Size Greater Than 0";
    public static final String FILE_CREATED_TODAY = "File Created Today";
    public static final String FILE_CREATED_YESTERDAY = "File Created Yesterday";
        
    public static final String[] FILE_CHECK_CONDITIONS = {
        FILE_EXISTS,
        FILE_DOES_NOT_EXIST,
        FILE_SIZE_GREATER_THAN_ZERO,
        FILE_CREATED_TODAY,
        FILE_CREATED_YESTERDAY
    };
    
    public static final String[] DEFAULT_UNNECCESSARY_TAGS = {
        "script",
        "link",
        "img",
        "meta",
        "input.type=image"
    };
    
    public static final Map <String, Class> XML_SCHEMA_TYPE_TO_JAVA_CLASS = new HashMap<String, Class>();
    
    static {
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("string", java.lang.String.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("integer", java.math.BigInteger.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("int", java.lang.Integer.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("long", java.lang.Long.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("short", java.lang.Short.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("decimal", java.math.BigDecimal.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("float", java.lang.Float.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("double", java.lang.Double.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("boolean", java.lang.Boolean.class);
        XML_SCHEMA_TYPE_TO_JAVA_CLASS.put("dateTime", java.util.Calendar.class);
    }
    
    public static final Set <String> HTTP_REQUEST_HEADERS_TO_IGNORE = new HashSet<String>();
    
    static {
        HTTP_REQUEST_HEADERS_TO_IGNORE.add(HttpHeaders.USER_AGENT);
        HTTP_REQUEST_HEADERS_TO_IGNORE.add(HttpHeaders.CONTENT_LENGTH);
        HTTP_REQUEST_HEADERS_TO_IGNORE.add(HttpHeaders.COOKIE);
        HTTP_REQUEST_HEADERS_TO_IGNORE.add(HttpHeaders.TRANSFER_ENCODING);
    }
    
    public static final String DAILY = "daily";
    public static final String HOURLY = "hourly";
    public static final String WEEKLY = "weekly";
    public static final String MONTHLY = "monthly";

    public static final String[] TEST_REPEAT_INTERVALS = {
        "",
        HOURLY,
        DAILY,
        WEEKLY,
        MONTHLY
    };
    
    public static final String[] URL_ENCODING_CHARACTERS = {
        "%08",
        "%09",
        "%0A",
        "%0D",
        "%20",
        "%21",
        "%22",
        "%23",
        "%24",
        "%25",
        "%26",
        "%27",
        "%28",
        "%29",
        "%2A",
        "%2B",
        "%2C",
        "%2D",
        "%2E",
        "%2F"
    };
    
    public static String[] SUBMIT_INPUT_TYPES = {
        "submit",
        "image"
    };
}