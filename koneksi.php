<?php
// $serverName = "stmik-yadika-bangil.database.windows.net"; //serverName\instanceName

// // Since UID and PWD are not specified in the $connectionInfo array,
// // The connection will be attempted using Windows Authentication.
// $connectionInfo = array( "Database"=>"uas-aplikasimobile7-db", "UID"=>"stmikyadikabangil", "PWD"=>"!5tm1ky4d1k4b4n91l#");
// $conn = sqlsrv_connect( $serverName, $connectionInfo);

// if( $conn ) {
//      echo "Connection established.<br />";
// }else{
//      echo "Connection could not be established.<br />";
//      die( print_r( sqlsrv_errors(), true));
// }

// -------------------------------------
// Perform database operations here.
// -------------------------------------

// Close the connection.
// sqlsrv_close( $conn );


// PHP Data Objects(PDO) Sample Code:
try {
    $conn = new PDO("sqlsrv:server = tcp:stmik-yadika-bangil.database.windows.net,1433; Database = uas-aplikasimobile7-db", "stmikyadikabangil", "{your_password_here}");
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
}
catch (PDOException $e) {
    print("Error connecting to SQL Server.");
    die(print_r($e));
}

// SQL Server Extension Sample Code:
$connectionInfo = array("UID" => "stmikyadikabangil", "pwd" => "!5tm1ky4d1k4b4n91l#", "Database" => "uas-aplikasimobile7-db", "LoginTimeout" => 30, "Encrypt" => 1, "TrustServerCertificate" => 0);
$serverName = "tcp:stmik-yadika-bangil.database.windows.net,1433";
$conn = sqlsrv_connect($serverName, $connectionInfo);
?>
