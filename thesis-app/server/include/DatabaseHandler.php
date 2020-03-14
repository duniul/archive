<?php

header('Content-Type: application/json; charset=utf-8');

class DatabaseHandler {

    private $logger;
    private $db;

    // Constructor
    function __construct() {
        require_once 'include/Logger.php';
    	require_once 'include/DatabaseConfig.php';
        
        // Includes logger to enable logging.
        $this->logger = new Logger();

    	// Connects to the database.
    	$this->db = new mysqli(DB_HOST, DB_USERNAME, DB_PASSWORD, DB_DATABASE);
    	if($this->db->connect_errno > 0){
    		die('Unable to connect to database [' . $db->connect_error . ']');
    	}

    	// Sets charset to UTF-8 to allow swedish characters.
    	$this->db->set_charset("utf8");
    }

    // Destructor
    function __destruct() {
        $this->closeDatabase();
    }

    // Close connection.
    public function closeDatabase() {
        $this->db->close();
    }
    
    /**
     * Checks if a user has signed in with the current Google User ID before.
     */
    public function isGoogleAccountRegistered($googleID) {
        // Selects all users with the current Google User ID from the database.
        $sqlString = "SELECT `userID` FROM `users` WHERE `googleID` = ?";
        
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('s', $googleID);
            $statement->execute();
            $statement->store_result();

            $row = $statement->bind_result($userID);
            
            while($statement->fetch()) {
                return $userID;
            }
            
            $statement->free_result();
            $statement->close();
            
            return false;
            
        } else {
            $this->logger->write("DatabaseHandler->isGoogleAccountRegistered(): ERROR! Query preparation failed.");
            die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
    }
    
    /**
     * Checks if a user has signed in with the current Google User ID before.
     */
    public function isUserIDTaken($userID) {
        $sqlString = "SELECT * FROM `users` WHERE `userID` = ?";
        
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('s', $userID);
            $statement->execute();
            $statement->store_result();
            
            $numberOfRows = $statement->num_rows();
            $statement->free_result();
            $statement->close();
            
            // If there are one or more results the user has been saved before,
            // so return true. Otherwise return false.
            if ($numberOfRows > 0) {
                return true;
            } else {
                return false;
            }
            
        } else {
        	$this->logger->write("DatabaseHandler->isUserIDTaken(): ERROR! Query preparation failed.");
            die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
    }
    
    /**
     * Saves a new user to the database and gives it an internal user ID.
     */
     public function storeUser($googleID, $email) {
        $isTaken = true;

        // The default user ID is the Google ID without the first character, in order
        // for it to fit inside a BIGINT/Long.
        $userID = substr($googleID, 3);

        while ($isTaken) {
            if ($this->isUserIDTaken($userID)) {
                $userID = mt_rand(0, mt_getrandmax());
            } else {
                $isTaken = false;
            }
        }

        // Stores the Google ID and user ID to the database.
        $sqlString = "INSERT INTO users(googleID, userID, email) VALUES(?, ?, ?)";
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('sss', $googleID, $userID, $email);
            $statement->execute();
            $statement->close();
            
            // If there are one or more results the user has been saved before,
            // so return true. Otherwise return false.
            if ($this->isGoogleAccountRegistered($googleID)) {
                return $userID;
            } else {
                return false;
            }
            
        } else {
        	$this->logger->write("DatabaseHandler->storeUser(): ERROR! Query preparation failed.");
            die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
     }
     
     public function doesListeningSessionExist($sessionID) {
        $sqlString = "SELECT * FROM `listeningSessions` WHERE `sessionID` = ?";
        
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('s', $sessionID);
            $statement->execute();
            $statement->store_result();
            
            $numberOfRows = $statement->num_rows();
            $statement->free_result();
            $statement->close();

            if ($numberOfRows > 0) {
                return true;
            } else {
                return false;
            }
            
        } else {
        	$this->logger->write("DatabaseHandler->doesListeningSessionExist(): ERROR! Query preparation failed.");
            die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
     }
     
     public function storeListeningSession($userID, $sessionID, $startTime, $endTime) {
         
        $sqlString = "INSERT INTO listeningSessions(userID, sessionID, startTime, endTime) VALUES(?, ?, ?, ?)";
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('ssss', $userID, $sessionID, $startTime, $endTime);
            $statement->execute();
            $statement->close();

            if ($this->doesListeningSessionExist($sessionID)) {
                return true;
            } else {
                return false;
            }
            
        } else {
        	$this->logger->write("DatabaseHandler->storeListeningSession(): ERROR! Query preparation failed.");
            die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
     }
     
    public function doesRecordingExist($recordingID) {
        $sqlString = "SELECT * FROM `recordings` WHERE `recordingID` = ?";
        
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('s', $recordingID);
            $statement->execute();
            $statement->store_result();
            
            $numberOfRows = $statement->num_rows();
            $statement->free_result();
            $statement->close();

            if ($numberOfRows > 0) {
                return true;
            } else {
                return false;
            }
            
        } else {
        	$this->logger->write("DatabaseHandler->doesRecordingExist(): ERROR! Query preparation failed.");
            die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
     }
     
     public function storeRecordingInfo($sessionID, $recordingID, $recordingURL, $userFilename, $recordingStartDate, $recordingEndDate, $duration, $lastEdited) {
        $uploadDate = date('Y-m-d H:i:s');
        
        $sqlString = "INSERT INTO recordings(sessionID, recordingID, URL, uploadDate, userFilename, recordingStartDate, recordingEndDate, duration, lastEdited)
                      VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('sssssssis', $sessionID, $recordingID, $recordingURL, $uploadDate, $userFilename, $recordingStartDate, $recordingEndDate, $duration, $lastEdited);
            $statement->execute();
            $statement->close();
            
            if ($this->doesRecordingExist($recordingID)) {
                return array("recordingID" => $recordingID, "url" => $recordingURL, "uploadDate" => $uploadDate);
            } else {
                return false;
            }
            
        } else {
        	$this->logger->write("DatabaseHandler->storeRecordingInfo(): ERROR! Query preparation failed.");
        	die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
     }
     
    public function doesPositionExist($sessionID, $datetime) {
        $sqlString = "SELECT * FROM `positions` 
                      WHERE `sessionID` = ? AND `datetime` = ?";
        
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('ss', $sessionID, $datetime);
            $statement->execute();
            $statement->store_result();
            
            $numberOfRows = $statement->num_rows();
            $statement->free_result();
            $statement->close();

            if ($numberOfRows > 0) {
                return true;
            } else {
                return false;
            }
            
        } else {
            $this->logger->write("DatabaseHandler->doesPositionExist(): ERROR! Query preparation failed.");
            die("(DatabaseHandler->doesPositionExist) Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
    }
     
    public function storePosition($sessionID, $datetime, $latitude, $longitude) {
        // INSERT string to store position to database.
        $sqlString = "INSERT INTO positions(sessionID, datetime, latitude, longitude) 
                      VALUES(?, ?, ?, ?)";
        
        // Prepare and execute the statement.
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('ssdd', $sessionID, $datetime, $latitude, $longitude);
            $statement->execute();
            $statement->close();

            // Check if the new position has been added, return true if it has and false if not.
            return ($this->doesPositionExist($sessionID, $datetime));
            
        } else {
        	$this->logger->write("DatabaseHandler->storePosition(): ERROR! Query preparation failed.");
        	die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
    }
    
    public function getUserTables($userID) {
    	$response = array();
    	
    	$sqlStringListeningSessions = "SELECT `userID`, `sessionID`, `startTime`, `endTime`
    								   FROM `listeningSessions`
                      				   WHERE `userID` = ?";
    	
    	if ($statement = $this->db->prepare($sqlStringListeningSessions)) {
    		$statement->bind_param('s', $userID);
    		$statement->execute();
    		$statement->store_result();
    		$statement->bind_result($userID, $sessionID, $startTime, $endTime);
    		
    		$rowNumber = 0;
    		$response["listeningSessionsRows"] = array();
    		while ($statement->fetch()) {
    			$response["listeningSessionsRows"][$rowNumber]["userID"] = $userID;
    			$response["listeningSessionsRows"][$rowNumber]["sessionID"] = $sessionID;
    			$response["listeningSessionsRows"][$rowNumber]["startTime"] = $startTime;
    			$response["listeningSessionsRows"][$rowNumber]["endTime"] = $endTime;
    			
    			$rowNumber++;
    		}
    		
    		$statement->free_result();
    		$statement->close();

    	} else {
    		$this->logger->write("DatabaseHandler->getUserTables(): ERROR! Query preparation failed for listening sessions.");
    		die("Query preparation failed for listening sessions: (" . $this->db->errno . ") " . $this->db->error);
    	}
    	
    	$sqlStringRecordings = "SELECT recordings.sessionID, `recordingID`, `URL`, `uploadDate`, `userFilename`, `recordingStartDate`, `recordingEndDate`, `duration`, `lastEdited`
								FROM `listeningSessions`, `recordings` 
								WHERE listeningSessions.sessionID = recordings.sessionID 
								AND `userID`= ?";
    	
    	if ($statement = $this->db->prepare($sqlStringRecordings)) {
    		$statement->bind_param('s', $userID);
    		$statement->execute();
    		$statement->store_result();
    		$statement->bind_result($sessionID, $recordingID, $url, $uploadDate, $userFilename, $recordingStartDate, $recordingEndDate, $duration, $lastEdited);
    	
    		$rowNumber = 0;
    		$response["recordingsRows"] = array();
    		while ($statement->fetch()) {
    			$response["recordingsRows"][$rowNumber]["sessionID"] = $sessionID;
    			$response["recordingsRows"][$rowNumber]["recordingID"] = $recordingID;
    			$response["recordingsRows"][$rowNumber]["url"] = $url;
    			$response["recordingsRows"][$rowNumber]["uploadDate"] = $uploadDate;
    			$response["recordingsRows"][$rowNumber]["userFilename"] = $userFilename;
    			$response["recordingsRows"][$rowNumber]["recordingStartDate"] = $recordingStartDate;
    			$response["recordingsRows"][$rowNumber]["recordingEndDate"] = $recordingEndDate;
    			$response["recordingsRows"][$rowNumber]["duration"] = $duration;
    			$response["recordingsRows"][$rowNumber]["lastEdited"] = $lastEdited;
    			 
    			$rowNumber++;
    		}
    		
    		$statement->free_result();
    		$statement->close();
    	
    	} else {
    		$this->logger->write("DatabaseHandler->getUserTables(): ERROR! Query preparation failed for recordings.");
    		die("Query preparation failed for recordings: (" . $this->db->errno . ") " . $this->db->error);
    	}
    	
    	$sqlStringPositions = "SELECT positions.sessionID, `datetime`, `latitude`, `longitude`
    						   FROM `listeningSessions`, `positions`
    						   WHERE listeningSessions.sessionID = positions.sessionID
    						   AND `userID` = ?";
    	
    	if ($statement = $this->db->prepare($sqlStringPositions)) {
    		$statement->bind_param('s', $userID);
    		$statement->execute();
    		$statement->store_result();
    		$statement->bind_result($sessionID, $datetime, $latitude, $longitude);
    	
    		$rowNumber = 0;
    		$response["positionsRows"] = array();
    		while ($statement->fetch()) {
    			$response["positionsRows"][$rowNumber]["sessionID"] = $sessionID;
    			$response["positionsRows"][$rowNumber]["datetime"] = $datetime;
    			$response["positionsRows"][$rowNumber]["latitude"] = $latitude;
    			$response["positionsRows"][$rowNumber]["longitude"] = $longitude;
    			 
    			$rowNumber++;
    		}
    		
    		$statement->free_result();
    		$statement->close();
    	
    	} else {
    		$this->logger->write("DatabaseHandler->getUserTables(): ERROR! Query preparation failed for positions.");
    		die("Query preparation failed for positions: (" . $this->db->errno . ") " . $this->db->error);
    	}
    	
    	return $response;
    }
    
    public function checkListeningSession($sessionID, $endTime) {
        $sqlString = "SELECT `sessionID`
        			  FROM listeningSessions
                      WHERE `sessionID` = ?
        			  AND `endTime` = ?";
        
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('ss', $sessionID, $endTime);
            $statement->execute();
            $statement->store_result();
            
            $numberOfRows = $statement->num_rows();
            $statement->free_result();
            $statement->close();

            if ($numberOfRows > 0) {
                return true;
            } else {
                return false;
            }
            
        } else {
            $this->logger->write("DatabaseHandler->checkListeningSession(): ERROR! Query preparation failed.");
            die("(DatabaseHandler->checkListeningSession) Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
    }
    
    public function updateListeningSession($sessionID, $endTime) {
        $sqlString = "UPDATE listeningSessions
        			  SET `endTime` = ?
                      WHERE `sessionID` = ?";
        
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('ss', $endTime, $sessionID);
            $statement->execute();
            $statement->store_result();
            
            $result = $this->checkListeningSession($sessionID, $endTime);
            
            $statement->free_result();
            $statement->close();

            return $result;

        } else {
            $this->logger->write("DatabaseHandler->updateListeningSession(): ERROR! Query preparation failed.");
            die("(DatabaseHandler->updateListeningSession) Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
    }
    
    public function getRecordingServerPath($url) {
    	
            $sqlString = "SELECT `f
        			  	  FROM listeningSessions
                      	  WHERE `sessionID` = ?
        			  	  AND `endTime` = ?";
        
        if ($statement = $this->db->prepare($sqlString)) {
            $statement->bind_param('ss', $sessionID, $endTime);
            $statement->execute();
            $statement->store_result();
            
            $numberOfRows = $statement->num_rows();
            $statement->free_result();
            $statement->close();

            if ($numberOfRows > 0) {
                return true;
            } else {
                return false;
            }
            
        } else {
            $this->logger->write("DatabaseHandler->getRecordingServerPath(): ERROR! Query preparation failed.");
            die("(DatabaseHandler->getRecordingServerPath) Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
        }
    }
    
    public function deleteRecording($recordingID, $url) {
    	
    	$fileDeleted = false;
    	$serverPath = "/a/oberon-home1/h11/baak5928/public_html/audio_app_api/recordings/";
    	$fullPath = $serverPath . substr($url, 60);
    	
    	if(is_file($fullPath)) {
    		$fileDeleted = unlink($fullPath);
    	}
    	
    	$sqlString = "DELETE FROM recordings
        			  WHERE recordingID = ?";
    	
    	if ($statement = $this->db->prepare($sqlString)) {
    		$statement->bind_param('s', $recordingID);
    		$statement->execute();
    		$statement->store_result();
    	
    		$result = $this->doesRecordingExist($recordingID);
    	
    		$statement->free_result();
    		$statement->close();
    	
    		if ($result == false && $fileDeleted == true) {
    			return true;
    		} else {
    			return false;
    		}
    	
    	} else {
    		$this->logger->write("DatabaseHandler->deleteRecording(): ERROR! Query preparation failed.");
    		die("(DatabaseHandler->deleteRecording) Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
    	}
    }
    
    public function checkRecording($recordingID, $newFilename, $lastEdited) {
    	$sqlString = "SELECT `recordingID`
        			  FROM recordings
                      WHERE `recordingID` = ?
        			  AND `userFilename` = ?
    				  AND `lastEdited` = ?";
    
    	if ($statement = $this->db->prepare($sqlString)) {
    		$statement->bind_param('sss', $recordingID, $newFilename, $lastEdited);
    		$statement->execute();
    		$statement->store_result();
    
    		$numberOfRows = $statement->num_rows();
    		$statement->free_result();
    		$statement->close();
    
    		if ($numberOfRows > 0) {
    			return true;
    		} else {
    			return false;
    		}
    
    	} else {
    		$this->logger->write("DatabaseHandler->checkRecording(): ERROR! Query preparation failed.");
    		die("(DatabaseHandler->checkListeningSession) Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
    	}
    }
    
    public function updateRecording($recordingID, $newFilename, $lastEdited) {
    	$sqlString = "UPDATE recordings
        			  SET `userFilename` = ?, `lastEdited` = ?
                      WHERE `recordingID` = ?";
    	
    	if ($statement = $this->db->prepare($sqlString)) {
    		$statement->bind_param('sss', $newFilename, $lastEdited, $recordingID);
    		$statement->execute();
    		$statement->store_result();
    	
    		$result = $this->checkRecording($recordingID, $newFilename, $lastEdited);
    	
    		$statement->free_result();
    		$statement->close();
    	
    		return $result;
    	
    	} else {
    		$this->logger->write("DatabaseHandler->updateRecording(): ERROR! Query preparation failed.");
    		die("(DatabaseHandler->updateRecording) Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
    	}
    }
    
    public function deleteRecordingsFromServer($sessionID) {
    	
    	$sqlString = "SELECT `URL`
    				 FROM recordings
    				 WHERE `sessionID` = ?";
    	
    	if ($statement = $this->db->prepare($sqlString)) {
    		$statement->bind_param('s', $sessionID);
    		$statement->execute();
    		$statement->store_result();
    		$statement->bind_result($url);
    		
    		while ($statement->fetch()) {
    			$serverPath = "/a/oberon-home1/h11/baak5928/public_html/audio_app_api/recordings/";
    			$fullPath = $serverPath . substr($url, 60);
    			 
    			if(is_file($fullPath)) {
    				unlink($fullPath);
    			}
    		}
    		
    		$statement->free_result();
    		$statement->close();
    	
    		return true;
    	
    	} else {
    		$this->logger->write("DatabaseHandler->getRecordings(): ERROR! Query preparation failed.");
    		die("Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
    	}
    	
    }
    
    public function deleteSession($sessionID) {
    	
    	$this->deleteRecordingsFromServer($sessionID);
    	
    	$sqlString = "DELETE FROM listeningSessions
        			  WHERE sessionID = ?";
    	 
    	if ($statement = $this->db->prepare($sqlString)) {
    		$statement->bind_param('s', $sessionID);
    		$statement->execute();
    		$statement->store_result();
    		 
    		$result = $this->doesListeningSessionExist($sessionID);
    		 
    		$statement->free_result();
    		$statement->close();
    		 
    		if ($result == false) {
    			return true;
    		} else {
    			return false;
    		}
    		 
    	} else {
    		$this->logger->write("DatabaseHandler->deleteRecording(): ERROR! Query preparation failed.");
    		die("(DatabaseHandler->deleteRecording) Query preparation failed: (" . $this->db->errno . ") " . $this->db->error);
    	}
    	
    }
}
?>