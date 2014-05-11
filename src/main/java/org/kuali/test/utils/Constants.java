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

    
    public static final int DEFAULT_HORIZONTAL_DIVIDER_LOCATION = 150;
    public static final int DEFAULT_VERTICAL_DIVIDER_LOCATION = 300;
    public static final int MAINFRAME_DEFAULT_LEFT = 100;
    public static final int MAINFRAME_DEFAULT_TOP = 100;
    public static final int MAINFRAME_DEFAULT_WIDTH = 800;
    public static final int MAINFRAME_DEFAULT_HEIGHT = 600;
    
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
    
    
    public static final String HTTP_REQUEST_METHOD_GET = "GET";
    public static final String HTTP_REQUEST_METHOD_POST = "POST";
    public static final String HTTP_REQUEST_METHOD_CONNECT = "CONNECT";
    public static final String HTTP_RESPONSE_CONTENT_TYPE = "Content-Type";
    public static final String MIME_TYPE_HTML = "text/html";
    
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
        DEFAULT_HTML_CONTAINER_TAGS.add("span");
        
        
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
    public static final String HTML_TAG_TYPE_IFRAME = "iframe";
    public static final String HTML_TAG_ATTRIBUTE_FOR = "for";
    
    public static final String TEST_RUNNER_CONFIG_FILENAME = "test-runner-config.xml";
    
    
    public static final int DEFAULT_TEST_RUNNER_CONFIGURATION_UPDATE_INTERVAL = 15;
    public static final int DEFAULT_TEST_RUNNER_TEST_INQUIRY_INTERVAL = 10;
    public static final String[] DEFAULT_HTML_WHITELIST_TAGS = {
        "input", 
        "div", 
        "label", 
        "span", 
        "tr", 
        "th", 
        "td", 
        "select", 
        "option", 
        "iframe", 
        "body",
        "h2",
        "table",
        "tbody"
    };
    
    public static final String[] DEFAULT_HTML_WHITELIST_TAG_ATTRIBUTES = {
        "id", 
        "name", 
        "class", 
        "test-id"
    };
}
