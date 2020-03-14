<?php
header ('Content-Type: application/json; charset=utf-8', true);

// This file receives POST requests to store user GPS positions
// to the server.

// Include logger to enable logging.
require_once ('include/Logger.php');
$logger = new Logger();

// Include a database handler to use necessary functions.
require_once 'include/DatabaseHandler.php';
$databaseHandler = new DatabaseHandler();

// Initialize reponse array.
$response = array();
$response ["tag"] = "checkUser";

// Check if all necessary fields have been set.
if (isset($_POST['googleID']) && $_POST['googleID'] != '' && 
    isset($_POST['email']) && $_POST['email'] != '') {
	
	$postCopy = array();
	
	// Remove potential quotation marks from all received strings.
	$postCopy['googleID'] = str_replace("\"", "", $_POST['googleID']);
    $postCopy['email'] = str_replace("\"", "", $_POST['email']);

	
	// Check if the user has signed in before. If not, store the user in the database
    $returnedUserID = $databaseHandler->isGoogleAccountRegistered($postCopy['googleID']);
    if (!$returnedUserID) {
    	// If the Google ID haven't been registered before, store the ID and create an internal user ID.
    	$registeredUserID = $databaseHandler->storeUser($postCopy['googleID'], $postCopy['email']);
    	
    	if ($registeredUserID) {
    		// If the user was stored successfully, respond with the user ID.
    		$response["error"] = false;
    		$response["message"] = "Success: First time sign in! User has been stored.";
    		$response["userID"] = $registeredUserID;
    		$logger->write("check_user.php: New user was added with ID: " . $registeredUserID . ".");
    	} else {
    		// If the user couldn't be stored, respond with an error.
    		$response["error"] = true;
    		$response["message"] = "Error: Failed to store user.";
    		$response["userID"] = -1;
    		$logger->write("check_user.php: ERROR! Failed to store user new user.");
    	}
    
    } else {
    	// If the account is already registered, return userID immediately.
    	$response["error"] = false;
    	$response["message"] = "User is already stored.";
    	$response["userID"] = $returnedUserID;
    }
	
	// Encode and return the response.
	echo json_encode ($response);
} else {
	// If a variable is not set or is missing, respond with an error.
	$response["error"] = true;
	$response["message"] = "Error: Necessary POST variables are missing, null or empty.";
	$logger->write("store_position.php: ERROR! Necessary POST variables were missing, null or empty in a request.");
	
	// Encode and return the response.
	echo json_encode ($response);
}
?>