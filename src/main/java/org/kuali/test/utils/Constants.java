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
import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import org.kuali.test.creator.TestCreator;


public class Constants {
    public static final String DEFAULT_TIMESTAMP_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT_STRING = "yyyy-MM-dd";
    public static final String SELECT_DATETIME_FORMAT_STRING = "MM/dd/yyyy HH:mm";
    
    public static final SimpleDateFormat FILENAME_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    public static final SimpleDateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT_STRING);
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STRING);
    
    public static final ImageIcon KUALI_ICON = new ImageIcon(TestCreator.class.getResource("/images/kuali-small.png"));
    
    public static final ImageIcon REPOSITORY_ICON = new ImageIcon(TestCreator.class.getResource("/images/repository.png"));
    public static final ImageIcon PLATFORM_ICON = new ImageIcon(TestCreator.class.getResource("/images/platform.png"));
    public static final ImageIcon TEST_SUITE_ICON = new ImageIcon(TestCreator.class.getResource("/images/test-suite.png"));
    public static final ImageIcon TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/test.png"));
    public static final ImageIcon SAVE_CONFIGURATION_ICON = new ImageIcon(TestCreator.class.getResource("/images/save.png"));
    public static final ImageIcon CREATE_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/createtest.png"));
    public static final ImageIcon CANCEL_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/cancel-test.png"));

    public static final ImageIcon START_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/start-test.png"));
    public static final ImageIcon CREATE_CHECKPOINT_ICON = new ImageIcon(TestCreator.class.getResource("/images/create-checkpoint.png"));
    public static final ImageIcon SAVE_TEST_ICON = new ImageIcon(TestCreator.class.getResource("/images/save-test.png"));
    public static final ImageIcon CLOCK_ICON = new ImageIcon(TestCreator.class.getResource("/images/clock.png"));
    public static final ImageIcon CLOSE_TAB_ICON = new ImageIcon(TestCreator.class.getResource("/images/close-tab.png"));
    public static final ImageIcon ADD_ICON = new ImageIcon(TestCreator.class.getResource("/images/add.png"));
    public static final ImageIcon DELETE_ICON = new ImageIcon(TestCreator.class.getResource("/images/delete.png"));

    public static final ImageIcon DATABASE_ICON = new ImageIcon(TestCreator.class.getResource("/images/database.png"));
    public static final ImageIcon DATABASE_SETTING_ICON = new ImageIcon(TestCreator.class.getResource("/images/database-setting.png"));
    public static final ImageIcon DATABASE_TABLE_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbtable.png"));
    public static final ImageIcon DATABASE_TABLE_OUTER_JOIN_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbtable-outer-join.png"));
    public static final ImageIcon DATABASE_COLUMN_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbcolumn.png"));
    public static final ImageIcon DATABASE_PKCOLUMN_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbpkcolumn.png"));
    public static final ImageIcon DATABASE_COLUMN_SELECTED_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbcolumnsel.png"));
    public static final ImageIcon DATABASE_PKCOLUMN_SELECTED_ICON = new ImageIcon(TestCreator.class.getResource("/images/dbpkcolumnsel.png"));

    public static final int DEFAULT_HORIZONTAL_DIVIDER_LOCATION = 150;
    public static final int DEFAULT_VERTICAL_DIVIDER_LOCATION = 300;
    public static final int MAINFRAME_DEFAULT_LEFT = 100;
    public static final int MAINFRAME_DEFAULT_TOP = 100;
    public static final int MAINFRAME_DEFAULT_WIDTH = 800;
    public static final int MAINFRAME_DEFAULT_HEIGHT = 600;

    public static final int MAX_TABLE_RELATIONSHIP_DEPTH = 6;
    
    public static final String KUALI_TEST_TITLE = "Kuali Test Framework";
    public static final String PREFS_ROOT_NODE = "kualitest/mainframe";
    public static final String PREFS_TABLE_NODE = "kualitest/tables";
    public static final String PREFS_DLG_NODE = "kualitest/dialogs";
    public static final String PREFS_HORIZONTAL_DIVIDER_LOCATION = "horizontal-divider-location";
    public static final String PREFS_VERTICAL_DIVIDER_LOCATION = "vertical-divider-location";
    public static final String PREFS_MAINFRAME_LEFT = "mainframe-left";
    public static final String PREFS_MAINFRAME_TOP = "mainframe-top";
    public static final String PREFS_MAINFRAME_WIDTH = "mainframe-width";
    public static final String PREFS_MAINFRAME_HEIGHT = "mainframe-height";
    public static final String PREFS_MAINFRAME_WINDOW_STATE = "mainframe-window-state";
    public static final String COLOR_DARK_RED = "#680000";
    public static final String COLOR_DARK_BLUE = "#000099";
    public static final String HTML_MIME_TYPE = "text/html";
    
    public static final int DEFAULT_DIALOG_LEFT = 300;
    public static final int DEFAULT_DIALOG_TOP = 200;
    public static final String PREFS_DLG_LEFT = "-dlg-left";
    public static final String PREFS_DLG_TOP = "-dlg-top";
    public static final String PREFS_DLG_WIDTH = "-dlg-width";
    public static final String PREFS_DLG_HEIGHT = "-dlg-height";
    
    public static final String TEST_TRANSFER_MIME_TYPE = "test-transfer";
    
    
    public static final String SAVE_CONFIGURATION = "Save Configuration";
    public static final String CREATE_TEST = "Create Test";
    public static final String NEW_WEB_TEST = "New Web Test";
    public static final String TEST_OUTPUT = "Test Output";
    public static final String RUNNING_TESTS = "Running Tests";
    public static final String REPOSITORY = "Repository";
    public static final String DATABASES = "Databases";
    
    public static String SAVE_ACTION = "Save";
    public static String OK_ACTION = "Ok";
    public static String CANCEL_ACTION = "Cancel";
    public static String CONTINUE_ACTION = "Continue";
    public static final String START_TEST_ACTION = "Start Test";
    public static final String CANCEL_TEST_ACTION = "Cancel Test";
    public static final String CREATE_CHECKPOINT_ACTION = "Create Checkpoint";
    public static final String SAVE_TEST_ACTION = "Save Test";
    public static final String NEW_BROWSER_TAB_DEFAULT_TEXT = "new browser tab...";
    public static final String SCHEDULE_TEST_ACTION = "Schedule Test";
    public static final String REMOVE_TEST_ACTION = "Remove Test";
    public static final String PLATFORM_SELECTED_ACTION = "platform-selected";
    public static final String DEFAULT_PROXY_HOST= "localhost";
    public static final String DEFAULT_PROXY_PORT= "8888";
    public static final int MAX_RESPONSE_BUFFER_SIZE = (1024 * 1024);
    public static final int MAX_REQUEST_BUFFER_SIZE = (1024 * 1024);
    public static final int DEFAULT_SPLASH_WIDTH = 300;
    public static final int DEFAULT_SPLASH_HEIGHT = 75;
    public static final int DEFAULT_TABLE_COLUMN_WIDTH = 75;
    public static final int MESSAGE_DISPLAY_WORDS_PER_LINE = 20;
    public static final int DEFAULT_DISPLAY_TABLE_ROWS = 10;
    public static final String HEADER_INFO_PANEL_NAME = "Header Info";
    public static final String DEFAULT_HTML_PROPERTY_GROUP = "main";
    public static final String DEFAULT_HTML_PROPERTY_SECTION = "none";
    public static final String ROUTELOG_HTML_PROPERTY_GROUP = "RouteLog";
    public static final String NODE_INDEX_MATCHER_CODE = "I";
    public static final String NODE_ID = "nodeid";
    public static final String SYSTEM_PROPERTY_GROUP = "system";
    public static final String ADD_ROW_ACTION = "Add Row";
    public static final String DELETE_ROW_ACTION = "Delete Row";
    public static final String ADD_COLUMN_ACTION = "Add Column";
    public static final String DELETE_COLUMN_ACTION = "Delete Column";
    public static final String ADD_COMPARISON_ACTION = "Add Comparison";
    public static final String DELETE_COMPARISON_ACTION = "Delete Comparison";

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
    
    public static final String HTML_TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String TAB_SPACES = "    ";
    public static final String HTML_LINE_BREAK = "<br />";
    public static final String HTML_BOLD_UNDERLINE_STYLE = "<span style='font-weight: 650; white-space: nowrap; text-decoration: underline;'>^</span>";
    public static final String HTML_BOLD_BLUE_STYLE = "<span style='color: " + Constants.COLOR_DARK_BLUE + "; font-weight: 650; white-space: nowrap;'>^</span>";
    public static final String HTML_DARK_RED_STYLE = "<span style='color: " + Constants.COLOR_DARK_RED + "; white-space: nowrap;'>^</span>";
    
    public static final String JAVASCRIPT_SUFFIX = "js";
    public static final String CSS_SUFFIX = "css";
    public static final Set <String> IMAGE_SUFFIX_SET = new HashSet<String>();
    public static final Set <String> VALID_HTTP_REQUEST_METHOD_SET = new HashSet<String>();
    public static final Set <String> DEFAULT_HTML_CONTAINER_TAGS = new HashSet<String>();
    
    static {
        DEFAULT_HTML_CONTAINER_TAGS.add("body");
        DEFAULT_HTML_CONTAINER_TAGS.add("table");
        DEFAULT_HTML_CONTAINER_TAGS.add("tr");
        DEFAULT_HTML_CONTAINER_TAGS.add("iframe");
        DEFAULT_HTML_CONTAINER_TAGS.add("div");
        DEFAULT_HTML_CONTAINER_TAGS.add("tbody");
        
        
        IMAGE_SUFFIX_SET.add("gif");
        IMAGE_SUFFIX_SET.add("ico");
        IMAGE_SUFFIX_SET.add("jpg");
        IMAGE_SUFFIX_SET.add("jpeg");
        IMAGE_SUFFIX_SET.add("png");

        VALID_HTTP_REQUEST_METHOD_SET.add(HTTP_REQUEST_METHOD_GET);
        VALID_HTTP_REQUEST_METHOD_SET.add(HTTP_REQUEST_METHOD_POST);
    }
    

    public static final String HTML_TAG_TYPE_OPTION = "option";
    public static final String HTML_TAG_TYPE_LABEL = "label";
    public static final String HTML_TAG_TYPE_INPUT = "input";
    public static final String HTML_TAG_TYPE_IFRAME = "iframe";
    public static final String HTML_TAG_TYPE_TABLE = "table";
    public static final String HTML_TAG_TYPE_TR = "tr";
    public static final String HTML_TAG_TYPE_TH = "th";
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
    
    public static final String HTML_TAG_ATTRIBUTE_FOR = "for";
    public static final String HTML_TAG_ATTRIBUTE_ID = "id";
    public static final String HTML_TAG_ATTRIBUTE_NAME = "name";
    public static final String HTML_TAG_ATTRIBUTE_CLASS = "class";
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

    
    public static final String TEST_RUNNER_CONFIG_FILENAME = "test-runner-config.xml";
    
    
    public static final int DEFAULT_TEST_RUNNER_CONFIGURATION_UPDATE_INTERVAL = 15;
    public static final int DEFAULT_TEST_RUNNER_TEST_INQUIRY_INTERVAL = 10;
    public static final String[] DEFAULT_HTML_WHITELIST_TAGS = {
        HTML_TAG_TYPE_INPUT, 
        HTML_TAG_TYPE_DIV, 
        HTML_TAG_TYPE_LABEL, 
        HTML_TAG_TYPE_SPAN, 
        HTML_TAG_TYPE_TR, 
        HTML_TAG_TYPE_TH, 
        HTML_TAG_TYPE_TD, 
        HTML_TAG_TYPE_SELECT, 
        HTML_TAG_TYPE_OPTION, 
        HTML_TAG_TYPE_IFRAME, 
        HTML_TAG_TYPE_BODY,
        HTML_TAG_TYPE_H2,
        HTML_TAG_TYPE_TABLE,
        HTML_TAG_TYPE_TBODY,
        HTML_TAG_TYPE_TEXTAREA,
        HTML_TAG_TYPE_ANCHOR
    };
    
    public static final String[] DEFAULT_HTML_WHITELIST_TAG_ATTRIBUTES = {
        HTML_TAG_ATTRIBUTE_ID, 
        HTML_TAG_ATTRIBUTE_NAME, 
        HTML_TAG_ATTRIBUTE_CLASS, 
        NODE_ID
    };
    
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


    public static final String[] OPERATORS = {
        "=",
        "<",
        "<=",
        ">",
        ">=",
        "!=",
        "in",
        "like",
        "null",
        "not null"
    };
}
