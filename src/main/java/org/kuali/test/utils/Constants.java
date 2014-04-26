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

import javax.swing.ImageIcon;
import org.kuali.test.ui.KualiTestApp;


public class Constants {
    public static final ImageIcon KUALI_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/kuali-small.png"));
    
    public static final ImageIcon REPOSITORY_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/repository.png"));
    public static final ImageIcon PLATFORM_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/platform.png"));
    public static final ImageIcon TEST_SUITE_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/test-suite.png"));
    public static final ImageIcon TEST_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/test.png"));
    public static final ImageIcon SAVE_CONFIGURATION_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/save.png"));
    public static final ImageIcon CREATE_TEST_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/createtest.png"));

    public static final ImageIcon START_TEST_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/start-test.png"));
    public static final ImageIcon CREATE_CHECKPOINT_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/create-checkpoint.png"));
    public static final ImageIcon SAVE_TEST_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/save-test.png"));
    public static final ImageIcon CLOCK_ICON = new ImageIcon(KualiTestApp.class.getResource("/images/clock.png"));

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
    
    
    public static String SAVE_ACTION = "Save";
    public static String CANCEL_ACTION = "Cancel";
    public static String CONTINUE_ACTION = "Continue";
    public static final String START_TEST_ACTION = "Start Test";
    public static final String CREATE_CHECKPOINT_ACTION = "Create Checkpoint";
    public static final String SAVE_TEST_ACTION = "Save Test";

    public static final int TEST_PROXY_SERVER_PORT= 8888;
    public static final int PROXY_RESPONSE_FILTER_MAX_SIZE = (200 * 1024);
    public static final int DEFAULT_SPLASH_WIDTH = 300;
    public static final int DEFAULT_SPLASH_HEIGHT = 75;
    public static final int DEFAULT_TABLE_COLUMN_WIDTH = 75;
    public static final int MESSAGE_DISPLAY_WORDS_PER_LINE = 20;
    public static final int DEFAULT_DISPLAY_TABLE_ROWS = 10;
}
