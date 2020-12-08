<?php 
	include "koneksi.php";
	
	$query = sqlsrv_query($conn, "SELECT * FROM data_mhs");
	
	$json = array();
	
	while($row = sqlsrv_fetch_array($query)){
		$json[] = $row;
	}
	
	echo json_encode($json);
	
	mysqli_close($conn);
	
?>
