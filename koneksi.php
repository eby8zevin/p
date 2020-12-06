<?php
$serverName = "stmik-yadika-bangil.database.windows.net"; //serverName\instanceName

// Since UID and PWD are not specified in the $connectionInfo array,
// The connection will be attempted using Windows Authentication.
$connectionInfo = array( "Database"=>"uas-aplikasimobile7-db", "UID"=>"stmikyadikabangil", "PWD"=>"!5tm1ky4d1k4b4n91l#");
$conn = sqlsrv_connect( $serverName, $connectionInfo);

if( $conn ) {
     echo "Connection established.<br />";
}else{
     echo "Connection could not be established.<br />";
     die( print_r( sqlsrv_errors(), true));
}

//-------------------------------------
// Perform database operations here.
//-------------------------------------

// Close the connection.
//sqlsrv_close( $conn );
?>
