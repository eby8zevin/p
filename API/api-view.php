<?php 
	include "koneksi.php";
	
	$query = sqlsrv_query($conn, "SELECT * FROM data_mhs");
	
	$json = array();
	
	while($row = sqlsrv_fetch_array($query, SQLSRV_FETCH_ASSOC)){
		$json[] = $row;
	}
	
	echo json_encode($json);
	
	mysqli_close($conn);
	
?>
