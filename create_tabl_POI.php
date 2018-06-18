<?php
$servername = "localhost";
$username = "root";
$password = "At_f#_N-SoM";
$dbname = "ftDB";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error){
	die("Connection failed: " . $conn->connect_error);
}
$sql = "CREATE TABLE POI (
	id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	name text(512) NOT NULL,
	latitude float(30) NOT NULL,
	longitude float(30) NOT NULL,
	tags text(20),
	rating float(10),
	time float(10)
)";
if($conn->query($sql) === TRUE)
{
	echo "Table created successfully";
}
else
{
	echo "Error creating table: " . $conn->error;
}

