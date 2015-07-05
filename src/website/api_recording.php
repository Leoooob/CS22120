<?php
/*
 * Add reserve to database from POST HTTP request
 * todo:	- confirm date format
 					- ask tom about abundance
 */

$aber_user = "pus1";	//set this to your username if hosting in your own public_html
											//also make sure uploads directory exists with write permissions (ie. ~/public_html/uploads/)

$servername = "db.dcs.aber.ac.uk";
$username = "admjof26";
$password = "sCcb6Kbw";

// connect to database
$conn = new mysqli($servername, $username, $password);
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
} 
$conn->select_db("csgp09_14_15");

var_dump($_POST);

if (isset($_POST['reserve_id'])) {
	$reserve_id = intval($_POST['reserve_id']);
	$location_lat = floatval($_POST['location_lat']);
	$location_lon = floatval($_POST['location_lon']);
	$abundance = $conn->real_escape_string($_POST['abundance'])[0];
	$date =  date("Y-m-d H:i:s", $_POST['date']);
	$comment = $conn->real_escape_string($_POST['comment']);
	$species_name_latin = $conn->real_escape_string($_POST['species_name']);

	//upload general_photo
	if (isset($_FILES['general_photo'])) {
		//check for error in upload
		if ($_FILES['general_photo']['error'] > 0) {
			die('An error occurred when uploading.');
		}
		//check that file is under 5mb
		if ($_FILES['general_photo']['size'] > 5000000) {
			die('File uploaded exceeds max upload size (5mb)');
		}
		//set unique filename
		$filename = uniqid() . $_FILES['general_photo']['name'];
		//upload file
		if (!move_uploaded_file($_FILES['general_photo']['tmp_name'], '../uploads/' . $filename)) {
			die('Error uploading file, check destination is writeable.');
		}
		//set image url
		$general_photo_url = $conn->real_escape_string("http://users.aber.ac.uk/" . $aber_user . "/uploads/" . $filename);
	}

	//upload specimen_photo
	if (isset($_FILES['specimen_photo'])) {
		//check for error in upload
		if ($_FILES['specimen_photo']['error'] > 0) {
			die('An error occurred when uploading.');
		}
		//check that file is under 5mb
		if ($_FILES['specimen_photo']['size'] > 5000000) {
			die('File uploaded exceeds max upload size (5mb)');
		}
		//set unique filename
		$filename = uniqid() . $_FILES['specimen_photo']['name'];
		//upload file (checking for error)
		if (!move_uploaded_file($_FILES['specimen_photo']['tmp_name'], '../uploads/' . $filename)) {
			die('Error uploading file, check destination is writeable.');
		}
		//set image url
		$specimen_photo_url = $conn->real_escape_string("http://users.aber.ac.uk/" . $aber_user . "/uploads/" . $filename);
	}

	//if species is already in database, use id of existing species
	$result = $conn->query("SELECT id,name_species FROM Species");
	if ($result->num_rows > 0) {
		while ($row = $result->fetch_assoc()) {
			if ($row['name_species'] == $species_name_latin) {
				$species_id = $row['id'];
				break;
			}
		}
	}

	//if species does not already exist, create new species entry
	if (!isset($species_id)) {
		$species_name_common = $conn->real_escape_string($_POST['species_common_name']);
		$species_authority = $conn->real_escape_string($_POST['species_authority']);

		$species_query = "INSERT INTO Species (Species.name_common, Species.name_species, Species.authority)
			VALUES ('$species_name_common', '$species_name_latin', '$species_authority')";
		if ($conn->query($species_query)) {
			$species_id = $conn->insert_id;
		}
	}

	$recording_query = "INSERT INTO Species_Recordings 
		(Species_Recordings.reserve_id, Species_Recordings.species_id, Species_Recordings.location_lat, Species_Recordings.location_lon,
			Species_Recordings.abundance, Species_Recordings.date, Species_Recordings.comment, Species_Recordings.general_photo_url,
			Species_Recordings.specimen_photo_url) 
		VALUES ('$reserve_id', '$species_id', '$location_lat', '$location_lon', '$abundance', '$date', '$comment', '$general_photo_url', 
			'$specimen_photo_url')";

	var_dump($recording_query);

	if ($conn->query($recording_query)) {
		echo $conn->insert_id;
	}
}

?>