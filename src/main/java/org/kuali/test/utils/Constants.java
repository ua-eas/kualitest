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
    public static final SimpleDateFormat FILENAME_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    public static final SimpleDateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    
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

    public static final String DEFAULT_PROXY_HOST= "localhost";
    public static final String DEFAULT_PROXY_PORT= "8888";
    public static final int MAX_RESPONSE_BUFFER_SIZE = (1024 * 1024);
    public static final int MAX_REQUEST_BUFFER_SIZE = (1024 * 1024);
    public static final int DEFAULT_SPLASH_WIDTH = 300;
    public static final int DEFAULT_SPLASH_HEIGHT = 75;
    public static final int DEFAULT_TABLE_COLUMN_WIDTH = 75;
    public static final int MESSAGE_DISPLAY_WORDS_PER_LINE = 20;
    public static final int DEFAULT_DISPLAY_TABLE_ROWS = 10;
    
    public static final String HTTP_REQUEST_METHOD_GET = "GET";
    public static final String HTTP_REQUEST_METHOD_POST = "POST";
    public static final String HTTP_REQUEST_METHOD_CONNECT = "CONNECT";
    public static final String HTTP_RESPONSE_CONTENT_TYPE = "Content-Type";
    public static final String MIME_TYPE_HTML = "text/html";
    
    public static final String JAVASCRIPT_SUFFIX = "js";
    public static final String CSS_SUFFIX = "css";
    public static final Set <String> IMAGE_SUFFIX_SET = new HashSet<String>();
    public static final Set <String> VALID_HTTP_REQUEST_METHOD_SET = new HashSet<String>();
    public static final Set <String> VALID_CHECKPOINT_TAG_TYPES = new HashSet<String>();
    
    static {
        IMAGE_SUFFIX_SET.add("gif");
        IMAGE_SUFFIX_SET.add("ico");
        IMAGE_SUFFIX_SET.add("jpg");
        IMAGE_SUFFIX_SET.add("jpeg");
        IMAGE_SUFFIX_SET.add("png");

        VALID_HTTP_REQUEST_METHOD_SET.add(HTTP_REQUEST_METHOD_GET);
        VALID_HTTP_REQUEST_METHOD_SET.add(HTTP_REQUEST_METHOD_POST);

        VALID_CHECKPOINT_TAG_TYPES.add("input");
        VALID_CHECKPOINT_TAG_TYPES.add("select");
    }
    
    
    public static final long TEST_RUNNER_CHECK_INTERVAL = 60*1000;
    
    public static final String HTML_TAG_TYPE_INPUT = "input";
    public static final String HTML_INPUT_TYPE_IMAGE = "image";
    public static final String HTML_INPUT_TYPE_HIDDEN = "hidden";
    public static final String HTML_INPUT_TYPE_SUBMIT = "submit";
    public static final String HTML_INPUT_TYPE_RADIO = "radio";
    public static final String HTML_INPUT_TYPE_CHECKBOX = "checkbox";
    public static final String HTML_TAG_TYPE_SELECT = "select";
    public static final String HTML_TAG_TYPE_OPTION = "option";
}
