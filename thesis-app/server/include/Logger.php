<?php

class Logger {
    
    function __construct() {
        
    }
    
    public function write($message) {
        
        $fullMessage = "(" . date('Y-m-d H:i:s') .") " . $message . "\n";
        
        error_log($fullMessage, 
                  3, 
                  "/a/oberon-home1/h11/baak5928/public_html/audio_app_api/com_log.log");
    }
}

?>