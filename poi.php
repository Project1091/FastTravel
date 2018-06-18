<?php
include 'connection_ft.php';
$const_latitude = "0.00090299";
$const_longitude = "0.00160833";
$tags = $_GET['t'];
$latitude_1 = $_GET['la'];
$longitude_1 = $_GET['lo'];
$time = $_GET['ti'];
$tag = explode(",",$tags);
$radius = ($time/60)*4;
$raznica = $radius*1000/100;
$latitude_plus = $latitude_1 + ($const_latitude*$raznica);
$latitude_minus = $latitude_1 - ($const_latitude*$raznica);
$longitude_plus = $longitude_1 + ($const_longitude*$raznica);
$longitude_minus = $longitude_1 - ($const_longitude*$raznica);
$query = "SELECT id, name, latitude, longitude, tags, rating, time FROM POI WHERE (tags='$tag[0]' OR tags='$tag[1]' OR tags='$tag[2]') AND longitude >= '$longitude_minus' AND longitude <= '$longitude_plus' AND latitude >= '$latitude_minus' AND latitude <= '$latitude_plus'";// ORDER BY rating DESC";
$result = $conn->query($query);
$row = $result->fetch_assoc(); 
if($result->num_rows>0)
{
	while($row=$result->fetch_assoc())
	{	
	 	$gori = array( 
		"ID"=>$row[id],
		"Name"=>$row[name],
		"Latitude"=>$row[latitude],
		"Longitude"=>$row[longitude],
		"Tags"=>$row[tags],
		"Rating"=>$row[rating],
		"Time"=>$row[time],
		"Dlina"=>$dlina=sqrt(pow(($latitude_1-$row[latitude]),2)+pow(($longitude_1-$row[longitute]),2))
		);
		$limit++;
		if($limit>40)
		{
			exit(1);
		}
		echo json_encode($gori,JSON_UNESCAPED_SLASHES|JSON_PRETTY_PRINT|JSON_UNESCAPED_UNICODE|JSON_FORCE_OBJECT);
		}
	}
?>
